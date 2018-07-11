package com.jani.houses;

import com.jani.houses.output.ImmutableTeaser;
import com.jani.houses.output.Teaser;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static io.vavr.API.Try;

public enum OffersProvider {

    GRATKA("https://gratka.pl/nieruchomosci/domy?rodzaj-ogloszenia=sprzedaz&lokalizacja_region=%C5%82%C3%B3dzkie" +
        "&lokalizacja_miejscowosc=lodz&lokalizacja_dzielnica=polesie&page=") {

        private static final String DIV_PAGINATION_CLASS = "div[class=pagination]";
        private static final String INPUT_PAGINATION = "input[id^=pagination]";
        private static final String A_TEASER_CLASS = "a[class^=teaser]";
        private static final String MAX = "max";
        private static final String ID = "id";
        private static final String TITLE = "title";

        @Override
        Option<Integer> maxPageIndex(Document mainPage) {
            return Try(() ->
                mainPage
                    .select(DIV_PAGINATION_CLASS)
                    .last()
                    .select(INPUT_PAGINATION)
                    .attr(MAX))
                .map(Integer::parseInt)
                .onFailure(this::error)
                .toOption();
        }

        @Override
        public Stream<Teaser> extractTeasersFromPage(Document page) {
            return Stream.ofAll(page.select(A_TEASER_CLASS).stream())
                .map(this::createTeaser);
        }

        ImmutableTeaser createTeaser(Element teaserElement) {
            String html = teaserElement.select("p[class=teaser__price]").html();
            return ImmutableTeaser.builder()
                .id(teaserElement.attr(ID))
                .title(teaserElement.attr(TITLE))
                .price(html.substring(0, html.indexOf("<span>")).trim())
                .url(teaserElement.attr(HREF))
                .build();
        }
    },
    OTODOM("https://www.otodom.pl/sprzedaz/dom/lodz/polesie/?search%5Bdescription%5D=1&search%5Bdist%5D=0&" +
        "search%5Bdistrict_id%5D=112&search%5Bsubregion_id%5D=127&search%5Bcity_id%5D=1004&page=") {

        private static final String LI_PAGER_COUNTER_CLASS = "li[class=pager-counter]";
        private static final String STRONG_CURRENT_CLASS = STRONG + "[class=current]";
        private static final String ARTICLE_OFFER_ITEM_CLASS = "article[class^=offer-item]";
        private static final String ANCHOR_WITH_DATA_ID = "a[" + DATA_ID + "]";
        private static final String HEADER_OFFER_DETAILS_CLASS = "header[class=offer-item-header]";
        private static final String SPAN_OFFER_ITEM_TITLE_CLASS = "span[class=offer-item-title]";
        private static final String LI_OFFER_ITEM_PRICE = "li[class=offer-item-price]";

        @Override
        Option<Integer> maxPageIndex(Document mainPage) {
            return Try(() -> mainPage
                .select(LI_PAGER_COUNTER_CLASS)
                .select(STRONG_CURRENT_CLASS)
                .text())
                .map(Integer::parseInt)
                .onFailure(this::error)
                .toOption();
        }

        @Override
        public Stream<Teaser> extractTeasersFromPage(Document page) {
            return Stream.ofAll(page.select(ARTICLE_OFFER_ITEM_CLASS).stream())
                .map(this::createTeaser);
        }

        ImmutableTeaser createTeaser(Element teaserElement) {
            Element anchorObserved = teaserElement.select(HEADER_OFFER_DETAILS_CLASS).select(ANCHOR).first();
            return ImmutableTeaser.builder()
                .id(teaserElement.select(ANCHOR_WITH_DATA_ID).attr(DATA_ID))
                .title(anchorObserved.select(SPAN_OFFER_ITEM_TITLE_CLASS).text())
                .price(teaserElement.select(LI_OFFER_ITEM_PRICE).first().text().trim())
                .url(anchorObserved.attr(HREF))
                .build();
        }
    },
    OLX("https://www.olx.pl/nieruchomosci/domy/lodz/?search%5Bdistrict_id%5D=295&page=") {

        private static final String DIV_PAGER_REL_CRL_CLASS = "div[class*=pager rel clr]";
        private static final String ANCHOR_BLOCK_CLASS = "a[class*=block]";
        private static final String SPAN = "a[class*=block]";
        private static final String TABLE_OFFERS = "table[id=offers_table]";
        private static final String TABLE_DATA_ID_INSIDE_TD_CLASS_OFFER = "td[class^=offer] table[data-id]";
        private static final String DIV_SPACE_REL_CLASS = "div[class=space rel]";
        private static final String STRONG_INSIDE_P_PRICE_CLASS = "p[class=price] strong";

        @Override
        Option<Integer> maxPageIndex(Document mainPage) {
            return Try(() -> mainPage
                .select(DIV_PAGER_REL_CRL_CLASS)
                .select(ANCHOR_BLOCK_CLASS)
                .last()
                .select(SPAN)
                .text())
                .map(Integer::parseInt)
                .toOption();
        }

        @Override
        public Stream<Teaser> extractTeasersFromPage(Document page) {
            return Stream.ofAll(
                page
                    .select(TABLE_OFFERS)
                    .select(TABLE_DATA_ID_INSIDE_TD_CLASS_OFFER)
                    .stream()
                    .filter(OFFER_TABLE_SELECTOR)
            ).map(this::createTeaser);

        }

        ImmutableTeaser createTeaser(Element teaserElement) {
            Elements divSpaceRelClass = teaserElement.select(DIV_SPACE_REL_CLASS);
            Element titleAndUrlAnchor = divSpaceRelClass.select(ANCHOR).first();
            Element priceParagraph = teaserElement.select(STRONG_INSIDE_P_PRICE_CLASS).first();

            return ImmutableTeaser.builder()
                .id(teaserElement.attr(DATA_ID))
                .title(titleAndUrlAnchor.select(STRONG).text())
                .price(priceParagraph.text())
                .url(titleAndUrlAnchor.attr(HREF))
                .build();
        }
    },
    MORIZON("https://www.morizon.pl/domy/lodz/polesie/?page=") {

        @Override
        Option<Integer> maxPageIndex(Document mainPage) {
            return Try(() -> {
                Elements lis = mainPage
                    .select("ul[class^=nav nav-pills mz-pagination-number]")
                    .select("li");
                Element lastLiWithNumber = lis.get(lis.size() - 2);
                return lastLiWithNumber
                    .text();
            }).toOption()
                .map(Integer::parseInt);
        }

        @Override
        public Stream<Teaser> extractTeasersFromPage(Document page) {
            return Stream.ofAll(
                page
                    .select("div[class=contentBox]")
                    .select("div[class*=row row--property-list][data-id]")
                    .stream()
            ).map(this::createTeaser);
        }

        ImmutableTeaser createTeaser(Element teaserElement) {
            return ImmutableTeaser.builder()
                .id(teaserElement.attr(DATA_ID))
                .title(teaserElement.select("h2[class$=__title]").text())
                .price(teaserElement.select("p[class$=__price]").text())
                .url(teaserElement.select("a[href=property_link]").attr("href"))
                .build();
        }
    };

    static final String HREF = "href";
    static final String DATA_ID = "data-id";
    static final String ANCHOR = "a";
    static final String STRONG = "strong";

    public static final Predicate<Element> OFFER_TABLE_SELECTOR =
        table ->
            table.getElementsByTag("span")
                .stream()
                .anyMatch(e -> e.text().contains("Łódź, Polesie"));

    private final Logger logger = LoggerFactory.getLogger(OffersProvider.class);

    final String baseUrl;

    OffersProvider(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String welcomePageUrl() {
        return pageNumberToUrl(1);
    }

    public String pageNumberToUrl(int number) {
        return baseUrl + number;
    }

    void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }

    abstract Option<Integer> maxPageIndex(Document mainPage);

    public abstract Stream<Teaser> extractTeasersFromPage(Document page);
}


