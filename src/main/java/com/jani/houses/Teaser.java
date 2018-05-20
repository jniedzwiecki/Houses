package com.jani.houses;

public class Teaser {

    private final String id;
    private final String title;

    static Teaser teaser(String id, String title) {
        return new Teaser(id, title);
    }

    private Teaser(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    @Override
    public String toString() {
        return String.format("ID: %s; title: %s", id, title);
    }
}