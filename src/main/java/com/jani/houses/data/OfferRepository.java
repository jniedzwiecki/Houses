package com.jani.houses.data;

import com.jani.houses.Teaser;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;

public abstract class OfferRepository implements CrudRepository<Offer, String> {

    private static final Logger logger = LoggerFactory.getLogger(OfferRepository.class);

    public void insertOrUpdateTeasers(List<Teaser> teasers) {
        logger.info("Storing {} teasers", teasers.size());

        LocalDateTime now = LocalDateTime.now();

        List<ImmutableOffer> offers = teasers.map(teaser ->
            ImmutableOffer.builder()
                .id(teaser.id())
                .title(teaser.title())
                .url(teaser.url())
                .insertionTime(now)
                .updateTime(now)
                .build()
        );

        saveAll(offers);
    }

    public List<Offer> queryInsertedOffers() {
        logger.info("Querying teasers.");

        return Stream.ofAll(findAll())
            .filter(offer -> offer.updates() == 0)
            .toList();
    }
}
