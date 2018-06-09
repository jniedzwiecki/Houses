package com.jani.houses.data;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.net.URL;
import java.time.LocalDateTime;

import static io.vavr.API.Try;
import static io.vavr.API.Tuple;

@Entity
public class Offer {
    private static final int NO_UPDATES = 0;

    @Transient
    private Logger logger = LoggerFactory.getLogger(Offer.class);

    static Offer offer(
        String id,
        String title,
        String url,
        LocalDateTime insertionTime,
        LocalDateTime updateTime) {
        return new Offer(id, title, url, insertionTime, updateTime, NO_UPDATES);
    }

    private Offer(String id, String title, String url, LocalDateTime insertionTime, LocalDateTime updateTime, int updates) {
        this.id = id;
        this.title = title;
        this.insertionTime = insertionTime;
        this.updateTime = updateTime;
        this.url = url;
        this.updates = updates;
    }

    public Offer() {
    }

    @Id
    private String id;

    private String title;

    private String url;

    private LocalDateTime insertionTime;

    private LocalDateTime updateTime;

    private int updates;

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

    String url() {
        return url;
    }

    int updates() {
        return updates;
    }

    void refreshUpdate(LocalDateTime now) {
        updateTime = now;
        updates++;
    }

    private void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }
}
