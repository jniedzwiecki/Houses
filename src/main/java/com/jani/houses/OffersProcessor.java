package com.jani.houses;

import com.jani.houses.properties.ApplicationProperties;
import com.jani.houses.properties.ModifiableApplicationProperties;
import io.vavr.API;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.commons.mail.DefaultAuthenticator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.function.Predicate;

import static io.vavr.API.Try;
import static io.vavr.API.Tuple;
import static io.vavr.collection.Stream.rangeClosed;
import static java.util.function.Function.identity;

@Component
@EnableConfigurationProperties(ModifiableApplicationProperties.class)
@EnableScheduling
class OffersProcessor {
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String HREF = "href";
    private static final String A_TEASER_CLASS = "a[class^=teaser]";
    private static final String DIV_PAGINATION_CLASS = "div[class=pagination]";
    private static final String GRATKA = "https://gratka.pl/nieruchomosci/domy?rodzaj-ogloszenia=sprzedaz&lokalizacja_region=%C5%82%C3%B3dzkie&lokalizacja_miejscowosc=lodz&lokalizacja_dzielnica=polesie&page=";
    private static final String INPUT_PAGINATION = "input[id^=pagination]";
    private static final String MAX = "max";

    private static final Logger logger = LoggerFactory.getLogger(OffersProcessor.class);

    private static final List<String> excludes = API.List("Złotno", "Koziny", "Szaserów");
    private static final Predicate<Teaser> TEASER_FILTER = teaser ->
        excludes.filter(excl -> teaser.title().contains(excl)).isEmpty();

    private final ApplicationProperties applicationProperties;

    OffersProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Scheduled(fixedRate = 600000)
    void browse() {
        maxPageIndex()
            .peek((Integer maxIndex) -> {
                List<Teaser> teasers =
                    List.ofAll(rangeClosed(1, maxIndex)
                        .map(this::pageNumberToUrl)
                        .map(this::getPage)
                        .filter(Option::isDefined)
                        .map(Option::get)
                        .flatMap(this::extractTeasersOnPage)
                        .filter(TEASER_FILTER)
                    );

                insertOrUpdateTeasers(teasers);

                List<Tuple2<URL, String>> tuple2s = queryInsertedTeasers();
                Option.when(tuple2s.nonEmpty(), tuple2s)
                    .map(insertedTeasers ->
                        ImmutableEmail.builder()
                            .to(API.List(applicationProperties.messaging().to()))
                            .from(applicationProperties.messaging().from())
                            .subject(applicationProperties.messaging().subject())
                            .authenticator(
                                new DefaultAuthenticator(applicationProperties.messaging().from(),
                                    applicationProperties.messaging().password())
                            )
                            .hostname(applicationProperties.messaging().hostname())
                            .contents(insertedTeasers)
                            .build()
                    )
                    .forEach(e -> Try(e::send).onFailure(this::error).toOption());
            })
            .onEmpty(() -> error(new IllegalStateException("Could not load paging document.")));
    }

    private Option<Integer> maxPageIndex() {
        return getPage(GRATKA + Integer.toString(1))
            .map(document -> document
                .select(DIV_PAGINATION_CLASS)
                .last()
                .select(INPUT_PAGINATION)
                .attr(MAX))
            .map(Integer::parseInt);
    }

    private String pageNumberToUrl(Integer page) {
        return GRATKA + page;
    }

    private Option<Document> getPage(String url) {
        logger.info("Connecting: {}", url);
        return Try(() -> Jsoup.connect(url).get())
            .onFailure(this::error)
            .toOption();
    }

    private void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }

    private Stream<Teaser> extractTeasersOnPage(Document document) {
        return extractTeasers(document.select(A_TEASER_CLASS));
    }

    private Stream<Teaser> extractTeasers(Elements teaserElements) {
        return Stream.ofAll(teaserElements.stream())
            .map(teaser -> ImmutableTeaser.builder()
                .id(teaser.attr(ID))
                .title(teaser.attr(TITLE))
                .url(teaser.attr(HREF))
                .build());
    }

    private void insertOrUpdateTeasers(List<Teaser> teasers) {
        String insertQuery = "insert into offers (offer_id, title, added_time, visited_time, url) "
            + "values (?, ?, ?, ?, ?) "
            + "on duplicate key update visited_time = ?, updates = updates + 1";

        Try.withResources(() -> DriverManager.getConnection(
            applicationProperties.database(),
            "houses",
            "eif(4es2")
        ).of(connection -> {
                teasers
                    .forEach(teaser -> {
                            logger.info("Storing teaser id={} into db", teaser.id());
                            Try.of(() -> preparedStatement(teaser, connection, insertQuery).executeUpdate())
                                .onFailure(this::error)
                                .toOption();
                        }
                    );
                return null;
            }
        ).onFailure(this::error);
    }

    private List<Tuple2<URL, String>> queryInsertedTeasers() {
        String offerQuery = "select * from offers where updates = 0";

        return Try.withResources(() -> DriverManager.getConnection(
            applicationProperties.database(), "houses", "eif(4es2")
        )
            .of(connection ->
                Try.of(() -> {
                    ResultSet resultSet = insertedTeasersStatement(offerQuery, connection).executeQuery();
                    List<Tuple2<URL, String>> offers = API.List();

                    while (resultSet.next()) {
                        String title = resultSet.getString(2);
                        String url = resultSet.getString(5);
                        offers = offers.append(Tuple(new URL(url), title));
                    }

                    return offers;
                })
                    .onFailure(this::error)
                    .toOption()
            )
            .onFailure(this::error)
            .toOption()
            .flatMap(identity())
            .getOrElse(List.empty());
    }

    private PreparedStatement insertedTeasersStatement(String offerQuery, Connection connection)
        throws SQLException {
        return connection.prepareStatement(offerQuery);
    }

    private PreparedStatement preparedStatement(Teaser teaser, Connection conn, String query)
        throws SQLException {
        Timestamp currentTime = Timestamp.from(new Date().toInstant());

        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, teaser.id());
        preparedStmt.setString(2, teaser.title());
        preparedStmt.setTimestamp(3, currentTime);
        preparedStmt.setTimestamp(4, currentTime);
        preparedStmt.setString(5, teaser.url());
        preparedStmt.setTimestamp(6, currentTime);

        return preparedStmt;
    }
}
