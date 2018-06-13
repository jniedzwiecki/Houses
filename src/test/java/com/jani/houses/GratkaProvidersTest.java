package com.jani.houses;

import com.jani.houses.output.Teaser;
import io.vavr.collection.Stream;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.jani.houses.OffersProvider.GRATKA;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class GratkaProvidersTest {

    @Value("classpath:basicExtractTeasersTest.html")
    private Resource basicExtractTeasersTestResource;

    @Value("classpath:gratkaProviderTest.html")
    private Resource gratkaProviderTestResource;

    private static final int BASIC_TEASER_NUMBER = 3;
    private static final int TEASER_NUMBER = 32;

    @Test
    void basicExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(basicExtractTeasersTestResource));
        Stream<Teaser> teasers = GRATKA.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(BASIC_TEASER_NUMBER);
    }

    @Test
    void gratkaExtractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(gratkaProviderTestResource));
        Stream<Teaser> teasers = GRATKA.extractTeasersFromPage(document);

        assertThat(teasers)
            .hasSize(TEASER_NUMBER);
    }

    private String loadHtml(Resource extractTeasersHtml) throws IOException {
        try (InputStream inputStream = extractTeasersHtml.getInputStream()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }
}
