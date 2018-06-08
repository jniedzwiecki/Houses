package com.jani.houses.data;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.net.URL;
import java.time.LocalDateTime;

import static io.vavr.API.Try;
import static io.vavr.API.Tuple;

@Entity
@Value.Immutable
public interface Offer {

    Logger logger = LoggerFactory.getLogger(Offer.class);

    @Id
    String id();

    String title();

    LocalDateTime insertionTime();

    LocalDateTime updateTime();

    String url();

    int updates();

    default Option<Tuple2<URL, String>> toEmailContent() {
        Option<URL> url = Try(() -> new URL(url())).onFailure(this::error).toOption();
        return url.map(u -> Tuple(u, title()));
    }

    default List<Tuple2<URL, String>> offersListToEmailContents(List<Offer> offers) {
        return offers
            .map(Offer::toEmailContent)
            .filter(Option::isDefined)
            .map(Option::get);
    }

    default void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }
}
