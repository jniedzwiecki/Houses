package com.jani.houses;

import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import static com.jani.houses.Teaser.teaser;
import static io.vavr.API.Try;

@SpringBootApplication
@EnableScheduling
public class HousesApplication {

    private static final String DATABASE = "jdbc:mysql://localhost/houses?serverTimezone=CET";

    private static final Logger logger = LoggerFactory.getLogger(HousesApplication.class);

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String URL = "href";
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
            .peek(maxIndex ->
                Stream.rangeClosed(1, maxIndex)
                    .map(page -> GRATKA + page)
                    .flatMap(HousesApplication::extractTeasersForPage)
                    .map(HousesApplication::updateTeaser)
                    .forEach(Try::get)
            )
            .getOrElseThrow(() -> new IllegalStateException("Could not download main document."));
    }

    private static Option<Integer> maxPageIndex() {
        return getDocument(GRATKA + Integer.toString(1))
            .map(document -> document
                .select(DIV_PAGINATION_CLASS)
                .last()
                .select(INPUT_PAGINATION)
                .attr(MAX))
            .map(Integer::parseInt);
    }

    private static Stream<Teaser> extractTeasersForPage(String url) {
        return getDocument(url)
            .map(document -> extractTeasers(document.select(A_TEASER_CLASS)))
            .getOrElse(Stream.empty());
    }

    private static Option<Document> getDocument(String url) {
        logger.info("Connecting: {}", url);
        return Try(() -> Jsoup.connect(url).get()).toOption();
    }

    private static Stream<Teaser> extractTeasers(Elements teaserElements) {
        return Stream.ofAll(teaserElements.stream())
            .map(teaser -> teaser(teaser.attr(ID), teaser.attr(TITLE), teaser.attr(URL)));
    }

    private static Try<Boolean> updateTeaser(Teaser teaser) {
        logger.info("Storing teaser id={} into db", teaser.id());

        return
            Try.withResources(() -> DriverManager.getConnection(DATABASE, "houses", "eif(4es2"))
                .of(conn -> {
                    String query = "insert into offers (offer_id, title, visited_time, url) "
                        + "values (?, ?, ?, ?) "
                        + "on duplicate key update visited_time = ?";

                    PreparedStatement preparedStmt = conn.prepareStatement(query);
                    preparedStmt.setString(1, teaser.id());
                    preparedStmt.setString(2, teaser.title());
                    preparedStmt.setTimestamp(3, Timestamp.from(new java.util.Date().toInstant()));
                    preparedStmt.setString(4, teaser.url());
                    preparedStmt.setTimestamp(5, Timestamp.from(new java.util.Date().toInstant()));

                    return preparedStmt.execute();
                });
    }
}
