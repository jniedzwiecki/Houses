package com.jani.houses;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.immutables.value.Value;

import javax.mail.Authenticator;
import java.net.URL;
import java.util.stream.Collectors;

import static io.vavr.API.Try;

@Value.Immutable
interface Email {
    String to();
    String from();
    String subject();
    List<Tuple2<URL, String>> contents();
    String hostname();
    Authenticator authenticator();

    default String send() throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(hostname());
        email.addTo(to());
        email.setFrom(from());
        email.setSubject(subject());
        email.setAuthenticator(authenticator());
        email.setSSLOnConnect(true);

        String embeddedImages = contents()
            .map(urlTitle -> Try(() -> email.embed(urlTitle._1, urlTitle._2)).getOrNull())
            .map(cid -> "<img src=\"cid:" + cid + "\">")
            .collect(Collectors.joining());

        email.setHtmlMsg("<html>" + embeddedImages + "</html>");

        email.setTextMsg("Your email client does not support HTML messages");

        return email.send();
    }
}
