package com.jani.houses;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.immutables.value.Value;

import javax.mail.Authenticator;
import java.net.URL;
import java.util.stream.Collectors;

@Value.Immutable
interface Email {
    List<String> toAddresses();
    String fromAddress();
    String subject();
    List<Tuple2<URL, String>> contents();
    String hostname();
    Authenticator authenticator();

    default String send() throws EmailException {
        SimpleEmail email = new SimpleEmail();
        email.setHostName(hostname());
        email.addTo(toAddresses().toJavaArray(String.class));
        email.setFrom(fromAddress());
        email.setSubject(subject());
        email.setAuthenticator(authenticator());
        email.setSSLOnConnect(true);

        String message = contents()
            .map(urlTitle -> urlTitle._2 + "\n" + urlTitle._1 + "\n\n")
            .collect(Collectors.joining());

        email.setMsg(message);

        return email.send();
    }
}
