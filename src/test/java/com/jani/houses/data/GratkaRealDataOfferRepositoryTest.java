package com.jani.houses.data;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class GratkaRealDataOfferRepositoryTest {

//    private static final String NEW_OFFER_ID = "offer-2961833";
//    private static final String NEW_OFFER_TITLE = "Dom Łódź Polesie, ul. Stare Złotno 83";
//    private static final String NEW_OFFER_URL =
//        "https://gratka.pl/nieruchomosci/dom-lodz-polesie-ul-stare-zlotno-83/oi/2961833";
//
//    private static final String ORIGINAL_OFFER_ID = "offer-4121411";
//    private static final String ORIGINAL_OFFER_TITLE = "Dom na ";
//    private static final String UPDATED_OFFER_TITLE = "Dom na  (zmiana ceny z 750 000 na 100 000)";
//    private static final String ORIGINAL_OFFER_URL = "https://gratka.pl/nieruchomosci/dom-na-zdrowiu-z-duza-dzialka/oi/4121411";
//    private static final String NEW_PRICE = "100 000";
//
//    @Value("classpath:gratkaNewOffersTestFirst.html")
//    private Resource gratkaNewOffersTestFirstResource;
//
//    @Value("classpath:gratkaNewOffersTestSecond.html")
//    private Resource gratkaNewOffersTestSecondResource;
//
//    @Value("classpath:gratkaNewOffersTestSecondNewPrice.html")
//    private Resource gratkaNewOffersTestSecondNewPriceResource;
//
//    @Autowired
//    private OfferRepository offerRepository;
//
//    @Test
//    void realDataFindNewOfferTest() throws IOException {
//        insertOrUpdateOffersFromDocument(gratkaNewOffersTestFirstResource, GRATKA);
//        insertOrUpdateOffersFromDocument(gratkaNewOffersTestSecondResource, GRATKA);
//
//        assertThat(offerRepository.queryInsertedOrUpdatedOffers().toJavaList())
//            .hasSize(1)
//            .first()
//            .extracting(Offer::id, Offer::title, Offer::url)
//            .contains(NEW_OFFER_ID, NEW_OFFER_TITLE, NEW_OFFER_URL);
//    }
//
//    @Test
//    void realDataFindOfferWithNewPriceTest() throws IOException {
//        insertOrUpdateOffersFromDocument(gratkaNewOffersTestFirstResource, GRATKA);
//        insertOrUpdateOffersFromDocument(gratkaNewOffersTestSecondNewPriceResource, GRATKA);
//
//        assertThat(offerRepository.queryInsertedOrUpdatedOffers().toJavaList())
//            .hasSize(1)
//            .first()
//            .extracting(Offer::id, Offer::title, Offer::price, Offer::url, Offer::updateInfo)
//            .contains(ORIGINAL_OFFER_ID, ORIGINAL_OFFER_TITLE, NEW_PRICE, ORIGINAL_OFFER_URL,
//                ImmutableUpdateInfo.builder()
//                    .title(UPDATED_OFFER_TITLE)
//                    .updated(true)
//                    .build()
//            );
//    }
//
//    private void insertOrUpdateOffersFromDocument(Resource html, OffersProvider offersProvider) throws IOException {
//        Document document = Jsoup.parse(loadHtml(html));
//        List<Offer> offers = offersProvider.extractTeasersFromPage(document)
//            .map(teaser -> offer(teaser.id(), teaser.title(), teaser.price(), teaser.url(), now(), now()))
//            .toList();
//        offerRepository.insertOrUpdateOffers(offers);
//    }
}
