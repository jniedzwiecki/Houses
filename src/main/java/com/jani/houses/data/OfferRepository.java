package com.jani.houses.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface OfferRepository extends CrudRepository<Offer, String>, OfferRepositoryCustom {
}
