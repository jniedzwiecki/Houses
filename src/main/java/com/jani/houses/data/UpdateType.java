package com.jani.houses.data;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.Validate.notNull;

enum UpdateType {

    NO_UPDATE(StringUtils.EMPTY) {
        @Override
        public UpdateInfo updateInfo(String... values) {
            throw new IllegalStateException("No message to complete");
        }
    },

    NEW_OFFER("Nowa oferta: %s") {
        @Override
        public UpdateInfo updateInfo(String... values) {
            String offerTitle = notNull(values[0]);
            return ImmutableUpdateInfo.builder()
                .title(String.format(message, offerTitle))
                .updated(true)
                .build();
        }
    },

    PRICE_CHANGE("%s (zmiana ceny z %s na %s)") {
        @Override
        public UpdateInfo updateInfo(String... values) {
            String offerTitle = notNull(values[0]);
            String originalPrice = notNull(values[1]);
            String newPrice = notNull(values[2]);
            return ImmutableUpdateInfo.builder()
                .title(String.format(message, offerTitle, originalPrice, newPrice))
                .updated(true)
                .build();
        }
    };

    protected final String message;

    UpdateType(String message) {
        this.message = message;
    }

    public abstract UpdateInfo updateInfo(String... values);
}