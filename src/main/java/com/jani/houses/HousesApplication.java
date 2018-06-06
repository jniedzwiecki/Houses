package com.jani.houses;

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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.function.Predicate;

import static io.vavr.API.List;
import static io.vavr.API.Try;
import static io.vavr.API.Tuple;
import static io.vavr.collection.Stream.rangeClosed;

@SpringBootApplication
@EnableScheduling
public class HousesApplication {

    private static final String DATABASE = "jdbc:mysql://localhost/houses?serverTimezone=CET";

    private static final Logger logger = LoggerFactory.getLogger(HousesApplication.class);

    private static final List<String> excludes = List("Złotno", "Koziny", "Szaserów");
    private static final Predicate<Teaser> TEASER_FILTER = teaser ->
        excludes.filter(excl -> teaser.title().contains(excl)).isEmpty();

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String HREF = "href";
    private static final String A_TEASER_CLASS = "a[class^=teaser]";
    private static final String DIV_PAGINATION_CLASS = "div[class=pagination]";
    private static final String GRATKA = "https://gratka.pl/nieruchomosci/domy?rodzaj-ogloszenia=sprzedaz&lokalizacja_region=%C5%82%C3%B3dzkie&lokalizacja_miejscowosc=lodz&lokalizacja_dzielnica=polesie&page=";
    private static final String INPUT_PAGINATION = "input[id^=pagination]";
    private static final String MAX = "max";

    public static void main(String[] args) {
        SpringApplication.run(HousesApplication.class, args);
    }

    @Scheduled(fixedRate = 600000)
    static void browse() {
        maxPageIndex()
            .peek((Integer maxIndex) -> {
                List<Teaser> teasers =
                    List.ofAll(rangeClosed(1, maxIndex)
                        .map(HousesApplication::pageNumberToUrl)
                        .map(HousesApplication::getPage)
                        .filter(Option::isDefined)
                        .map(Option::get)
                        .flatMap(HousesApplication::extractTeasersOnPage)
                        .filter(TEASER_FILTER)
                    );

                List<Tuple2<URL, String>> emailContentList =
                    updateTeasers(teasers)
                        .zip(teasers)
                        .filter(resultSetWithTeaser -> isInsertResultSet(resultSetWithTeaser._1))
                        .map(HousesApplication::teaser)
                        .map(HousesApplication::teaserToEmailContentItem)
                        .filter(Option::isDefined)
                        .map(Option::get);

                Email email = ImmutableEmail.builder()
                    .to("jniedzwiecki83@gmail.com")
                    .from("domyjacek@op.pl")
                    .subject("Domki")
                    .authenticator(new DefaultAuthenticator("domyjacek@op.pl", "jacekNNN666"))
                    .hostname("smtp.poczta.onet.pl")
                    .contents(emailContentList)
                    .build();

//                Try.of(email::send)
//                    .orElseRun(HousesApplication::error);
            })
            .onEmpty(() -> error(new IllegalStateException("Could not load paging document.")));
    }

    private static Option<Tuple2<URL, String>> teaserToEmailContentItem(Teaser teaser) {
        return Try(() -> new URL(teaser.url()))
            .onFailure(HousesApplication::error)
            .toOption()
            .map(u -> Tuple(u, teaser.title()));
    }

    private static Teaser teaser(Tuple2<Option<ResultSet>, Teaser> resultSetWithTeaser) {
        return resultSetWithTeaser._2();
    }

    private static boolean isInsertResultSet(Option<ResultSet> resultSet) {
        return resultSet.isDefined()
            && Try(() -> resultSet.get().rowInserted()).getOrElse(Boolean.FALSE);
    }

    private static String pageNumberToUrl(Integer page) {
        return GRATKA + page;
    }

    private static Option<Integer> maxPageIndex() {
        return getPage(GRATKA + Integer.toString(1))
            .map(document -> document
                .select(DIV_PAGINATION_CLASS)
                .last()
                .select(INPUT_PAGINATION)
                .attr(MAX))
            .map(Integer::parseInt);
    }

    private static Stream<Teaser> extractTeasersOnPage(Document document) {
        return extractTeasers(document.select(A_TEASER_CLASS));
    }

    private static Option<Document> getPage(String url) {
        logger.info("Connecting: {}", url);
        return Try(() -> Jsoup.connect(url).get())
            .onFailure(HousesApplication::error)
            .toOption();
    }

    private static void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }

    private static Stream<Teaser> extractTeasers(Elements teaserElements) {
        return Stream.ofAll(teaserElements.stream())
            .map(teaser -> ImmutableTeaser.builder()
                .id(teaser.attr(ID))
                .title(teaser.attr(TITLE))
                .url(teaser.attr(HREF))
                .build());
    }

    private static List<Option<ResultSet>> updateTeasers(List<Teaser> teasers) {
        String query = "insert into offers (offer_id, title, added_time, visited_time, url) "
            + "values (?, ?, ?, ?, ?) "
            + "on duplicate key update visited_time = ?";

        return Try.withResources(() -> DriverManager.getConnection(DATABASE, "houses", "eif(4es2"))
            .of(connection ->
                teasers
                    .peek(teaser -> logger.info("Storing teaser id={} into db", teaser.id()))
                    .map(
                        teaser ->
                            Try.of(() -> preparedStatement(teaser, connection, query).executeQuery())
                                .onFailure(HousesApplication::error)
                                .toOption()
                    )
            )
            .onFailure(HousesApplication::error)
            .getOrElse(List.empty());
    }

    private static PreparedStatement preparedStatement(Teaser teaser, Connection conn, String query)
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
