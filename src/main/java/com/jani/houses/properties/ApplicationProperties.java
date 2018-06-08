package com.jani.houses.properties;

import org.immutables.value.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;

@ConfigurationProperties
@Value.Modifiable
@Value.Style(create = "new", passAnnotations = ConfigurationProperties.class)
public interface ApplicationProperties {
    ModifiableMessagingProperties messaging();
    ModifiableDatabaseProperties database();
    ArrayList<String> excludes();

    @Value.Modifiable
    interface MessagingProperties {
        String from();
        ArrayList<String> to();
        String subject();
        String hostname();
        String password();
    }

    @Value.Modifiable
    interface DatabaseProperties {
        String url();
        String name();
        String password();
    }
}
