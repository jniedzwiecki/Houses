package com.jani.houses.data;

import org.immutables.value.Value;

@Value.Immutable
interface UpdateInfo {

    String title();
    boolean updated();
}
