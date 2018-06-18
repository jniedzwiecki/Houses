package com.jani.houses.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static com.jani.houses.data.Offer.offer;
import static io.vavr.API.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class OfferRepositoryTest {

    private static final String OFFER_1_ID = "offer0001";
    private static final String OFFER_2_ID = "offer0002";
    private static final String OFFER_1_TITLE = "offer1Title";
    private static final String OFFER_2_TITLE = "offer2Title";
    private static final String OFFER_1_URL = "http://provider.com/offer0001";
    private static final String OFFER_2_URL = "http://provider.com/offer0002";

    private static final Supplier<Offer> FIRST_OFFER =
        () -> offer(OFFER_1_ID, OFFER_1_TITLE, OFFER_1_URL, LocalDateTime.now(), LocalDateTime.now());

    private static final Supplier<Offer> SECOND_OFFER =
        () -> offer(OFFER_2_ID, OFFER_2_TITLE, OFFER_2_URL, LocalDateTime.now(), LocalDateTime.now());

    @Autowired
    private OfferRepository offerRepository;

    @Test
    void queryForNewOffersReturnsNoOffersWhenRepositoryEmpty() {
        assertThat(offerRepository.queryInsertedOffers())
            .isEmpty();
    }

    @Test
    void queryForNewOffersReturnsOneOfferWhenOneNewOfferInRepository() {
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER.get()));

        assertThat(offerRepository.queryInsertedOffers().toJavaList())
            .hasSize(1)
            .first()
            .extracting(Offer::id, Offer::title, Offer::url)
            .contains(OFFER_1_ID, OFFER_1_TITLE, OFFER_1_URL);
    }

    @Test
    void queryForNewOffersReturnsNoOffersIfExistingOfferWasOnceUpdated() {
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER.get()));
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER.get()));

        assertThat(offerRepository.queryInsertedOffers())
            .isEmpty();
    }

    @Test
    void queryForNewOffersReturnsFirstOffersIfSecondOfferWasOnceUpdated() {
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER.get()));
        offerRepository.insertOrUpdateOffers(List(SECOND_OFFER.get()));
        offerRepository.insertOrUpdateOffers(List(SECOND_OFFER.get()));

        assertThat(offerRepository.queryInsertedOffers())
            .hasSize(1)
            .first()
            .extracting(Offer::id, Offer::title, Offer::url)
            .contains(OFFER_1_ID, OFFER_1_TITLE, OFFER_1_URL);
    }

    @Test
    void queryForNewOffersReturnsSecondOffersIfFirstOfferWasOnceUpdated() {
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER.get()));
        offerRepository.insertOrUpdateOffers(List(FIRST_OFFER.get()));
        offerRepository.insertOrUpdateOffers(List(SECOND_OFFER.get()));

        assertThat(offerRepository.queryInsertedOffers())
            .hasSize(1)
            .first()
            .extracting(Offer::id, Offer::title, Offer::url)
            .contains(OFFER_2_ID, OFFER_2_TITLE, OFFER_2_URL);
    }
}
