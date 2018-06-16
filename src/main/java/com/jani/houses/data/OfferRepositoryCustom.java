package com.jani.houses.data;

import io.vavr.collection.List;

public interface OfferRepositoryCustom {
    void insertOrUpdateOffers(List<Offer> offers);
    List<Offer> queryInsertedOffers();
}
