package com.fhict.hololiveocgmanager;

import com.fhict.hololiveocgmanager.service.CardScraperService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HololiveOcgManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HololiveOcgManagerApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "app.scraper.run-on-startup", havingValue = "true", matchIfMissing = true)
    CommandLineRunner runInitialScrape(CardScraperService cardScraperService) {
        return args -> cardScraperService.scrapeAllCards();
    }
}
