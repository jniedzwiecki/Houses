package com.jani.houses.data;

import com.jani.houses.output.Teaser;
import io.vavr.collection.List;

public interface OfferRepositoryCustom {
    void insertOrUpdateTeasers(List<Teaser> teasers);
    List<Offer> queryInsertedOffers();
}
