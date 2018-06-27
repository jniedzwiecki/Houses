package com.jani.houses.data;

import org.immutables.value.Value;
import org.springframework.lang.Nullable;

import javax.persistence.Embeddable;

@Embeddable
@Value.Immutable
interface UpdateInfo {

    @Nullable
    String title();

    boolean updated();
}
