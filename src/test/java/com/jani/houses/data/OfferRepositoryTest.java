package com.jani.houses.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static com.jani.houses.data.Offer.offer;
import static io.vavr.API.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OfferRepositoryTest {

    private static final String OFFER_1_ID = "offer0001";
//    private static final String OFFER_2_ID = "offer0002";
    private static final String OFFER_1_TITLE = "offer1Title";
//    private static final String OFFER_2_TITLE = "offer2Title";
    private static final String OFFER_1_URL = "http://provider.com/offer0001";
//    private static final String OFFER_2_URL = "http://provider.com/offer0002";

    private static final Offer FIRST_OFFER =
        offer(OFFER_1_ID, OFFER_1_TITLE, OFFER_1_URL, LocalDateTime.now(), LocalDateTime.now());

//    private static final Offer SECOND_OFFER =
//        offer(OFFER_2_ID, OFFER_2_TITLE, OFFER_2_URL, LocalDateTime.now(), LocalDateTime.now());

    @Autowired
    private OfferRepository offerRepository;

    @Test
    void queryForNewOffersReturnsNoOffersWhenRepositoryEmpty() {
        assertThat(offerRepository.queryInsertedOffers())
            .isEmpty();
    }

    @Test
    void queryForNewOffersReturnsOneOfferWhenOneNewOfferInRepository() {
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER));

        Iterable<Offer> all = offerRepository.findAll();

        assertThat(offerRepository.queryInsertedOffers().toJavaList())
            .hasSize(1)
            .first()
            .extracting(Offer::id, Offer::title, Offer::url)
            .contains(OFFER_1_ID, OFFER_1_TITLE, OFFER_1_URL);

        offerRepository.deleteAll();
    }

    @Test
    void queryForNewOffersReturnsNoOffersIfExistingOfferWasOnceUpdated() {
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER));
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER));

        assertThat(offerRepository.queryInsertedOffers())
            .isEmpty();

        offerRepository.deleteAll();
    }
}
