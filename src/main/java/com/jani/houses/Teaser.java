package com.jani.houses;

public class Teaser {

    private final String id;
    private final String title;
    private final String url;

    static Teaser teaser(String id, String title, String url) {
        return new Teaser(id, title, url);
    }

    private Teaser(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
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

    @Override
    public String toString() {
        return String.format("ID: %s; title: %s, url: %s", id, title, url);
    }
}