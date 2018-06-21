package com.jani.houses.data;

import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.net.URL;
import java.time.LocalDateTime;

import static io.vavr.API.Try;
import static io.vavr.API.Tuple;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Entity
public class Offer {
    private static final int NO_UPDATES = 0;

    @Transient
    private Logger logger = LoggerFactory.getLogger(Offer.class);

    @Id
    @Column(length = 50)
    private String id;

    private String title;

    private String price;

    private String url;

    private LocalDateTime insertionTime;

    private LocalDateTime updateTime;

    private int updates;

    public static Offer offer(
        String id,
        String title,
        String price,
        String url,
        LocalDateTime insertionTime,
        LocalDateTime updateTime) {
        return new Offer(id, title, price, url, insertionTime, updateTime, NO_UPDATES);
    }

    private Offer(
        String id, String title, String price, String url, LocalDateTime insertionTime, LocalDateTime updateTime, int updates) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.insertionTime = insertionTime;
        this.updateTime = updateTime;
        this.url = url;
        this.updates = updates;
    }

    public Offer() { }

    public Option<Tuple2<URL, String>> toEmailContent() {
        Option<URL> url = Try(() -> new URL(url())).onFailure(this::error).toOption();
        return url.map(u -> Tuple(u, title()));
    }

    String id() {
        return id;
    }

    String title() {
        return title;
    }

    String price() {
        return price;
    }

    String url() {
        return url;
    }

    int updates() {
        return updates;
    }

    void update(Offer newOffer, LocalDateTime now) {
        if (StringUtils.equals(price, newOffer.price)) {
            refreshUpdates();
        } else {
            if (isNotEmpty(price)) {
                resetUpdates();
            }
            title = priceChangedTitle(newOffer);
            price = newOffer.price;
        }
        updateTime = now;
    }

    private void refreshUpdates() {
        updates++;
    }

    private void resetUpdates() {
        updates = 0;
    }

    private String priceChangedTitle(Offer newOffer) {
        return title + String.format(" (zmiana ceny z %s na %s)", price, newOffer.price);
    }

    private void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }
}
