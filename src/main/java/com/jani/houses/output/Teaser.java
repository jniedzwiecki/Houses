package com.jani.houses.output;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Teaser {
    public abstract String id();
    public abstract String title();
    public abstract String price();
    public abstract String url();

    @Override
    public String toString() {
        return String.format("ID: %s; title: %s, price: %s, url: %s", id(), title(), price(), url());
    }
}