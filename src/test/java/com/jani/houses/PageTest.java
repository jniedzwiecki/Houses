package com.jani.houses;

import io.vavr.collection.Stream;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(SpringRunner.class)
@SpringBootTest
class PageTest {

    @Value("classpath:extractTeasersTest.html")
    private Resource extractTeasersHtml;
    private static final int TEASER_NUMBER = 3;

    @Test
    void extractTeasersTest() throws IOException {
        Document document = Jsoup.parse(loadHtml(extractTeasersHtml));
        Page page = new Page(document);
        Stream<Teaser> teasers = page.extractTeasersFromPage();

        assertThat(teasers)
            .hasSize(TEASER_NUMBER);
    }

    private String loadHtml(Resource extractTeasersHtml) throws IOException {
        try (InputStream inputStream = extractTeasersHtml.getInputStream()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }
}
