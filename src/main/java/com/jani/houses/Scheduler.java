package com.jani.houses;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

    @Scheduled(fixedRate = 10000)
    void browsePages() {
        System.out.println("Dupa");
    }
}