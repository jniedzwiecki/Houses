package com.jani.houses;

import com.jani.houses.data.Offer;
import com.jani.houses.data.OfferRepository;
import com.jani.houses.output.Teaser;
import com.jani.houses.properties.ApplicationProperties;
import com.jani.houses.properties.ModifiableApplicationProperties;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.apache.commons.mail.DefaultAuthenticator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.jani.houses.output.ImmutableEmail;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import static com.jani.houses.data.Offer.offer;
import static io.vavr.API.List;
import static io.vavr.API.Option;
import static io.vavr.API.Some;
import static io.vavr.API.Try;
import static io.vavr.collection.List.rangeClosed;

@Component
@EnableConfigurationProperties(ModifiableApplicationProperties.class)
@EnableScheduling
class OffersProcessor {
    private static final String NO_NEW_CONTENT = "No new offers to send an email";
    private static final int DEFAULT_NUMBER_OF_PAGES = 1;

    private static final Logger logger = LoggerFactory.getLogger(OffersProcessor.class);

    private static final Function1<ApplicationProperties, Predicate<Teaser>> TEASER_FILTER =
        properties ->
            teaser ->
                List.ofAll(properties.excludes())
                    .filter(excl -> teaser.title().toLowerCase().contains(excl.toLowerCase()))
                    .isEmpty();

    private final ApplicationProperties applicationProperties;
    private final OfferRepository offerRepository;

    OffersProcessor(ApplicationProperties applicationProperties, OfferRepository offerRepository) {
        this.applicationProperties = applicationProperties;
        this.offerRepository = offerRepository;
    }

    @Scheduled(fixedRate = 600000)
    void browse() {
        LocalDateTime now = LocalDateTime.now();

        List(OffersProvider.values())
            .flatMap(this::teaserListFromProvider)
            .map(teasers -> teasers.map(teaser ->
                offer(teaser.id(), teaser.title(), teaser.price(), teaser.url(), now, now))
            )
            .forEach(offerRepository::insertOrUpdateOffers);

        Option<List<Offer>> lists = newOrUpdatedOffers()
            .onEmpty(() -> logger.info(NO_NEW_CONTENT));
//            .map(this::createEmailWithTeasers)
//            .map(email ->
//                Try(email::send)
//                    .onFailure(this::error));
    }

    private Option<List<Teaser>> teaserListFromProvider(OffersProvider offerProvider) {
        return downloadPage(offerProvider.welcomePageUrl())
            .onEmpty(() -> error(new IllegalStateException("Could not load document.")))
            .flatMap(mainPage ->
                offerProvider.maxPageIndex(mainPage)
                    .onEmpty(() -> info("Could not load number of pages."))
                    .orElse(() -> Some(DEFAULT_NUMBER_OF_PAGES))
                    .map(maxIndex ->
                        downloadSubpages(maxIndex, offerProvider)
                            .flatMap(offerProvider::extractTeasersFromPage)
                            .distinctBy(Teaser::id)
                            .filter(TEASER_FILTER.apply(applicationProperties)))
            );
    }

    private List<Document> downloadSubpages(Integer maxIndex, OffersProvider offersProvider) {
        return rangeClosed(1, maxIndex)
            .flatMap(pageNumber -> downloadPage(offersProvider.pageNumberToUrl(pageNumber)));
    }

    private Option<Document> downloadPage(String url) {
        logger.info("Connecting: {}", url);
        return Try(() -> Jsoup.connect(url).get())
            .onFailure(this::error)
            .toOption();
    }

    private Option<List<Offer>> newOrUpdatedOffers() {
        return Option(offerRepository.queryInsertedOrUpdatedOffers())
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

    private void info(String info) {
        logger.info(info);
    }
}
