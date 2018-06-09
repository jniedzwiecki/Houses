package com.jani.houses;

enum OffersProvider {

    GRATKA("https://gratka.pl/nieruchomosci/domy?rodzaj-ogloszenia=sprzedaz&lokalizacja_region=%C5%82%C3%B3dzkie" +
        "&lokalizacja_miejscowosc=lodz&lokalizacja_dzielnica=polesie&page=") {

        @Override
        public String pageNumberToUrl(int number) {
            return baseUrl + number;
        }
    };

    final String baseUrl;

    OffersProvider(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String welcomePageUrl() {
        return pageNumberToUrl(1);
    }

    public abstract String pageNumberToUrl(int number);
}
