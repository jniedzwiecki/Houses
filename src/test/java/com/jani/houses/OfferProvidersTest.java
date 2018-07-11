package com.jani.houses;

import com.jani.houses.output.Teaser;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.jani.houses.LoadHtml.loadHtml;
import static com.jani.houses.OffersProvider.GRATKA;
import static com.jani.houses.OffersProvider.MORIZON;
import static com.jani.houses.OffersProvider.OLX;
import static com.jani.houses.OffersProvider.OTODOM;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class OfferProvidersTest {

    @Value("classpath:basicGratkaExtractTeasersTest.html")
    private Resource basicGratkaExtractTeasersTestResource;

    @Value("classpath:gratkaProviderTest.html")
    private Resource gratkaProviderTestResource;

    @Value("classpath:basicOtodomExtractTeasersTest.html")
    private Resource basicOtodomExtractTeasersTestResource;

    @Value("classpath:otodomProviderTest.html")
    private Resource otodomProviderTestResource;

    @Value("classpath:basicOlxExtractTeasersTest.html")
    private Resource basicOlxExtractTeasersTestResource;

    @Value("classpath:olxProviderTest.html")
    private Resource olxProviderTestResource;

    @Value("classpath:morizonProviderTest.html")
    private Resource morizonProviderTestResource;

    private static final int BASIC_TEASER_NUMBER = 3;
    private static final int GRATKA_TEASER_NUMBER = 32;
    private static final int OTODOM_TEASER_NUMBER = 31;
    private static final int OLX_TEASER_NUMBER = 28;
    private static final int GRATKA_MAX_PAGE_INDEX = 5;
    private static final int OTODOM_MAX_PAGE_INDEX = 4;
    private static final int MORIZON_TEASER_NUMBER = 35;

    @Test
    void gratkaBasicExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(basicGratkaExtractTeasersTestResource));
        Stream<Teaser> teasers = GRATKA.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(BASIC_TEASER_NUMBER);
    }

    @Test
    void gratkaExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(gratkaProviderTestResource));
        Stream<Teaser> teasers = GRATKA.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(GRATKA_TEASER_NUMBER);
    }

    @Test
    void gratkaMaxPageNumberTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(gratkaProviderTestResource));
        Option<Integer> maxPageIndex = GRATKA.maxPageIndex(document);

        assertThat(maxPageIndex)
            .contains(GRATKA_MAX_PAGE_INDEX);
    }

    @Test
    void otodomBasicExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(basicOtodomExtractTeasersTestResource));
        Stream<Teaser> teasers = OTODOM.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(BASIC_TEASER_NUMBER);
    }

    @Test
    void otodomExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(otodomProviderTestResource));
        Stream<Teaser> teasers = OTODOM.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(OTODOM_TEASER_NUMBER);
    }

    @Test
    void otodomMaxPageNumberTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(otodomProviderTestResource));
        Option<Integer> maxPageIndex = OTODOM.maxPageIndex(document);

        assertThat(maxPageIndex)
            .contains(OTODOM_MAX_PAGE_INDEX);
    }

    @Test
    void olxBasicExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(basicOlxExtractTeasersTestResource));
        Stream<Teaser> teasers = OLX.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(BASIC_TEASER_NUMBER);
    }

    @Test
    void olxExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(olxProviderTestResource));
        Stream<Teaser> teasers = OLX.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(OLX_TEASER_NUMBER);
    }

    @Test
    void olxMaxPageNumberTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(olxProviderTestResource));
        Option<Integer> maxPageIndex = OLX.maxPageIndex(document);

        assertThat(maxPageIndex)
            .isEmpty();
    }

    @Test
    void morizonMaxPageNumberTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(morizonProviderTestResource));
        Option<Integer> maxPageIndex = MORIZON.maxPageIndex(document);

        assertThat(maxPageIndex)
            .contains(3);
    }

    @Test
    void morizonExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(morizonProviderTestResource));
        Stream<Teaser> teasers = MORIZON.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(MORIZON_TEASER_NUMBER);
    }
}
