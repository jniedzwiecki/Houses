package com.jani.houses;

enum OffersProvider {

    GRATKA("https://gratka.pl/nieruchomosci/domy?rodzaj-ogloszenia=sprzedaz&lokalizacja_region=%C5%82%C3%B3dzkie"
        + "&lokalizacja_miejscowosc=lodz&lokalizacja_dzielnica=polesie&page="),
    OTODOM("https://www.otodom.pl/sprzedaz/dom/lodz/polesie/?search%5Bdescription%5D=1&search%5Bdist%5D=0"
        + "&search%5Bdistrict_id%5D=112&search%5Bsubregion_id%5D=127&search%5Bcity_id%5D=1004&page="),
    OLX("https://www.olx.pl/nieruchomosci/domy/lodz/?search%5Bdistrict_id%5D=295&page=");

    final String baseUrl;

    OffersProvider(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String welcomePageUrl() {
        return pageNumberToUrl(1);
    }

    public String pageNumberToUrl(int number) {
        return baseUrl + number;
    }
}
