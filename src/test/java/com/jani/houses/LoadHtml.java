package com.jani.houses;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class LoadHtml {

    public static String loadHtml(Resource extractTeasersHtml) throws IOException {
        try (InputStream inputStream = extractTeasersHtml.getInputStream()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }
}
