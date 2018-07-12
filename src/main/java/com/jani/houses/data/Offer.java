package com.jani.houses.data;

import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.net.URL;
import java.time.LocalDateTime;

import static com.jani.houses.data.UpdateType.NEW_OFFER;
import static io.vavr.API.Try;
import static io.vavr.API.Tuple;

@Entity
public class Offer {

    @Transient
    private Logger logger = LoggerFactory.getLogger(Offer.class);

    @Id
    @Column(length = 50)
    private String id;

    private String title;

    private String price;

    private String url;

    @Embedded
    private UpdateInfo updateInfo;

    private LocalDateTime insertionTime;

    private LocalDateTime updateTime;

    public static Offer offer(
        String id,
        String title,
        String price,
        String url,
        LocalDateTime insertionTime,
        LocalDateTime updateTime) {
        return new Offer(id, title, price, url, insertionTime, updateTime);
    }

    private Offer(
        String id,
        String title,
        String price,
        String url,
        LocalDateTime insertionTime,
        LocalDateTime updateTime
    ) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.insertionTime = insertionTime;
        this.updateTime = updateTime;
        this.url = url;
    }

    public Offer() { }

    public Option<Tuple2<URL, String>> toEmailContent() {
        Option<URL> url = Try(() -> new URL(url())).onFailure(this::error).toOption();
        return url.map(u -> Tuple(u, updateInfo.getTitle()));
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

    UpdateInfo updateInfo() {
        return updateInfo;
    }

    Offer prepareForPersisting() {
        updateInfo = NEW_OFFER.updateInfo(title);
        return this;
    }

    void performUpdate(Offer updatedOffer) {
        if (priceUpdated(updatedOffer)) {
            updateInfo = UpdateType.PRICE_CHANGE.updateInfo(title, price, updatedOffer.price);
            price = updatedOffer.price;
        } else {
            updateInfo = UpdateType.NO_UPDATE.updateInfo();
        }
        refreshUpdateTime();
    }

    private void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }

    private boolean priceUpdated(Offer updatedOffer) {
        return price != null
            && StringUtils.compare(price, updatedOffer.price) != 0;
    }

    private void refreshUpdateTime() {
        updateTime = LocalDateTime.now();
    }
}
