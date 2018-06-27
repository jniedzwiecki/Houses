package com.jani.houses.data;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static io.vavr.API.Option;

@Transactional
public class OfferRepositoryImpl implements OfferRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(OfferRepository.class);

    private final EntityManager entityManager;

    public OfferRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void insertOrUpdateOffers(List<Offer> offers) {
        logger.info("Storing/updating {} offers", offers.size());

        offers.forEach(this::persistOrUpdate);

        entityManager.flush();
    }

    @Override
    public List<Offer> queryInsertedOrUpdatedOffers() {
        logger.info("Querying teasers.");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Offer> criteriaQuery = criteriaBuilder.createQuery(Offer.class);
        Root<Offer> rootOffer = criteriaQuery.from(Offer.class);
        CriteriaQuery<Offer> allOffers = criteriaQuery.select(rootOffer);
        TypedQuery<Offer> query = entityManager.createQuery(allOffers);

        return Stream.ofAll(query.getResultList())
            .filter(offer -> offer.updateInfo().updated())
            .toList();
    }

    private void persistOrUpdate(Offer offer) {
        Option(entityManager.find(Offer.class, offer.id()))
            .onEmpty(() ->
                entityManager
                    .persist(offer.prepareForPersisting())
            )
            .forEach(existingOffer ->
                existingOffer.performUpdate(offer));
    }
}
