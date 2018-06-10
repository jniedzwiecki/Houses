package com.jani.houses;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static io.vavr.API.Try;

class Page {
    private static final Logger logger = LoggerFactory.getLogger(OffersProcessor.class);

    private static final String DIV_PAGINATION_CLASS = "div[class=pagination]";
    private static final String INPUT_PAGINATION = "input[id^=pagination]";
    private static final String A_TEASER_CLASS = "a[class^=teaser]";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String HREF = "href";
    private static final String MAX = "max";

    private final Document document;

    Page(Document document) {
        this.document = document;
    }

    Option<Integer> maxPageIndex() {
        return Try(() ->
            document
                .select(DIV_PAGINATION_CLASS)
                .last()
                .select(INPUT_PAGINATION)
                .attr(MAX))
            .map(Integer::parseInt)
            .onFailure(this::error)
            .toOption();
    }

    List<Teaser> teasers() {
        return extractTeasersFromPage()
            .toList();
    }

    private Stream<Teaser> extractTeasersFromPage() {
        return Stream.ofAll(document.select(A_TEASER_CLASS).stream())
            .map(this::createTeaser);
    }

    private ImmutableTeaser createTeaser(Element teaserElement) {
        return ImmutableTeaser.builder()
            .id(teaserElement.attr(ID))
            .title(teaserElement.attr(TITLE))
            .url(teaserElement.attr(HREF))
            .build();
    }

    private void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }
}
