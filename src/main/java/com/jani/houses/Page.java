package com.jani.houses;

import org.jsoup.nodes.Document;

class Page {
    private final Document document;

    Page(Document document) {
        this.document = document;
    }

    Document document() {
        return document;
    }
}
