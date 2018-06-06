package com.jani.houses;

import org.immutables.value.Value;

@Value.Immutable
abstract class Teaser {
    abstract String id();
    abstract String title();
    abstract String url();

    @Override
    public String toString() {
        return String.format("ID: %s; title: %s, url: %s", id(), title(), url());
    }
}