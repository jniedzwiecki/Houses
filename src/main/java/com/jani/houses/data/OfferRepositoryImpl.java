package com.jani.houses.data;

import io.vavr.API;
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
import java.time.LocalDateTime;

@Transactional
public class OfferRepositoryImpl implements OfferRepositoryCustom {

    static final Logger logger = LoggerFactory.getLogger(OfferRepository.class);

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
    public List<Offer> queryInsertedOffers() {
        logger.info("Querying teasers.");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Offer> criteriaQuery = criteriaBuilder.createQuery(Offer.class);
        Root<Offer> rootOffer = criteriaQuery.from(Offer.class);
        CriteriaQuery<Offer> allOffers = criteriaQuery.select(rootOffer);
        TypedQuery<Offer> query = entityManager.createQuery(allOffers);


        return Stream.ofAll(query.getResultList())
            .filter(offer -> offer.updates() == 0)
            .toList();
    }

    private void persistOrUpdate(Offer offer) {
        API.Option(entityManager.find(Offer.class, offer.id()))
            .onEmpty(() -> entityManager.persist(offer))
            .forEach(existingOffer -> existingOffer.refreshUpdate(LocalDateTime.now()));
    }
}
