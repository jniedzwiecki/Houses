package com.jani.houses;

import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.jani.houses.Teaser.teaser;
import static io.vavr.API.Try;

@SpringBootApplication
public class HousesApplication {

    private static final Logger logger = LoggerFactory.getLogger(HousesApplication.class);
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String A_TEASER_CLASS = "a[class^=teaser]";
    private static final String DIV_PAGINATION_CLASS = "div[class=pagination]";
    private static final String GRATKA = "https://gratka.pl/nieruchomosci/domy?rodzaj-ogloszenia=sprzedaz&lokalizacja_region=%C5%82%C3%B3dzkie&lokalizacja_miejscowosc=lodz&lokalizacja_dzielnica=polesie&page=";
    private static final String INPUT_PAGINATION = "input[id^=pagination]";
    private static final String MAX = "max";

    public static void main(String[] args) {
//        SpringApplication.run(HousesApplication.class, args);

        maxPageIndex()
            .peek(maxIndex -> {
                    Stream<Teaser> teasers = Stream.rangeClosed(1, maxIndex)
                        .map(page -> GRATKA + page)
                        .flatMap(HousesApplication::extractTeasersForPage);

                    teasers
                        .peek(teaser ->
                            Try(() -> updateTeaser(teaser))
                                .getOrElseThrow(() -> new IllegalStateException("Could not store teaser to db.")));
                }
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
        java.util.stream.Stream<Teaser> teaserStream =
            teaserElements.stream().map(teaser -> teaser(teaser.attr(ID), teaser.attr(TITLE)));
        return Stream.ofAll(teaserStream);
    }

    private static Try<Boolean> updateTeaser(Teaser teaser) throws ClassNotFoundException {
        logger.info("Storing teaser id={} into db", teaser.id());

        String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost/houses";
        Class.forName(myDriver);
        return Try.withResources(() -> DriverManager.getConnection(myUrl, "root", ""))
            .of(conn -> {
                String query = "insert into offers (offer_id, title) values (?, ?)";

                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, teaser.id());
                preparedStmt.setString(2, teaser.title());

                return preparedStmt.execute();
            });

    }
}
