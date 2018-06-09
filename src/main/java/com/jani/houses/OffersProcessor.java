package com.jani.houses;

import com.jani.houses.data.Offer;
import com.jani.houses.data.OfferRepository;
import com.jani.houses.properties.ApplicationProperties;
import com.jani.houses.properties.ModifiableApplicationProperties;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.apache.commons.mail.DefaultAuthenticator;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.function.Predicate;

import static com.jani.houses.OffersProvider.GRATKA;
import static io.vavr.API.Option;
import static io.vavr.API.Try;
import static io.vavr.collection.Stream.rangeClosed;

@Component
@EnableConfigurationProperties(ModifiableApplicationProperties.class)
@EnableScheduling
class OffersProcessor {
    private static final String NO_NEW_CONTENT = "No new offers to send an email";

    private static final Logger logger = LoggerFactory.getLogger(OffersProcessor.class);

    private static final Function1<ApplicationProperties, Predicate<Teaser>> TEASER_FILTER =
        properties ->
            teaser ->
                List.ofAll(properties.excludes())
                    .filter(excl -> teaser.title().contains(excl))
                    .isEmpty();

    private final ApplicationProperties applicationProperties;
    private final OfferRepository offerRepository;

    OffersProcessor(ApplicationProperties applicationProperties, OfferRepository offerRepository) {
        this.applicationProperties = applicationProperties;
        this.offerRepository = offerRepository;
    }

    @Scheduled(fixedRate = 600000)
    void browse() {
        getPage(GRATKA.welcomePageUrl())
            .peek(page ->
                page.maxPageIndex()
                    .map(maxIndex -> {
                        rangeClosed(1, maxIndex)
                            .forEach(index -> insertOrUpdateTeasersOnPage(index, GRATKA));

                        return newOffers()
                            .onEmpty(() -> logger.info(NO_NEW_CONTENT))
                            .map(this::createEmailWithTeasers)
                            .map(email ->
                                Try(email::send)
                                    .onFailure(this::error));
                    })
                    .onEmpty(() -> error(new IllegalStateException("Could not load number of pages.")))
            )
            .onEmpty(() -> error(new IllegalStateException("Could not load document.")));
    }

    private Option<Page> getPage(String url) {
        logger.info("Connecting: {}", url);
        return Try(() -> Jsoup.connect(url).get())
            .onFailure(this::error)
            .map(Page::new)
            .toOption();
    }

    private void insertOrUpdateTeasersOnPage(Integer integer, OffersProvider offersProvider) {
        getPage(offersProvider.pageNumberToUrl(integer))
            .map(p -> p.teasers(TEASER_FILTER.apply(applicationProperties)))
            .forEach(offerRepository::insertOrUpdateTeasers);
    }

    private Option<List<Offer>> newOffers() {
        return Option(offerRepository.queryInsertedOffers())
            .filter(List::nonEmpty);
    }

    private ImmutableEmail createEmailWithTeasers(List<Offer> insertedOffers) {
        logger.info("Creating email with {} offers.", insertedOffers.size());

        return ImmutableEmail.builder()
            .toAddresses(List.ofAll(applicationProperties.messaging().to()))
            .fromAddress(applicationProperties.messaging().from())
            .subject(applicationProperties.messaging().subject())
            .authenticator(
                new DefaultAuthenticator(applicationProperties.messaging().from(),
                    applicationProperties.messaging().password())
            )
            .hostname(applicationProperties.messaging().hostname())
            .contents(offersListToEmailContents(insertedOffers))
            .build();
    }

    private List<Tuple2<URL, String>> offersListToEmailContents(List<Offer> offers) {
        return offers
            .map(Offer::toEmailContent)
            .filter(Option::isDefined)
            .map(Option::get);
    }

    private void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }
}
