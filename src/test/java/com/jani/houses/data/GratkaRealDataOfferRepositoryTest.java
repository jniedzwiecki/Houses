package com.jani.houses.data;

import com.jani.houses.OffersProvider;
import io.vavr.collection.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.jani.houses.LoadHtml.loadHtml;
import static com.jani.houses.OffersProvider.GRATKA;
import static com.jani.houses.data.Offer.offer;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class GratkaRealDataOfferRepositoryTest {

    private static final String NEW_OFFER_ID = "offer-2961833";
    private static final String NEW_OFFER_TITLE = "Dom Łódź Polesie, ul. Stare Złotno 83";
    private static final String NEW_OFFER_URL = "https://gratka.pl/nieruchomosci/dom-lodz-polesie-ul-stare-zlotno-83" +
        "/oi/2961833";

    @Value("classpath:gratkaNewOffersTestFirst.html")
    private Resource gratkaNewOffersTestFirstResource;

    @Value("classpath:gratkaNewOffersTestSecond.html")
    private Resource gratkaNewOffersTestSecondResource;

    @Autowired
    private OfferRepository offerRepository;

    @Test
    void realDataFindNewOfferTest() throws IOException {
        insertOrUpdateOffersFromDocument(gratkaNewOffersTestFirstResource, GRATKA);
        insertOrUpdateOffersFromDocument(gratkaNewOffersTestSecondResource, GRATKA);

        assertThat(offerRepository.queryInsertedOffers().toJavaList())
            .hasSize(1)
            .first()
            .extracting(Offer::id, Offer::title, Offer::url)
            .contains(NEW_OFFER_ID, NEW_OFFER_TITLE, NEW_OFFER_URL);
    }

    private void insertOrUpdateOffersFromDocument(Resource html, OffersProvider offersProvider) throws IOException {
        Document document = Jsoup.parse(loadHtml(html));
        List<Offer> offers = offersProvider.extractTeasersFromPage(document)
            .map(teaser -> offer(teaser.id(), teaser.title(), teaser.url(), now(), now()))
            .toList();
        offerRepository.insertOrUpdateOffers(offers);
    }
}
