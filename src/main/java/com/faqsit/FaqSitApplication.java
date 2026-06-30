package com.faqsit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FaqSitApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaqSitApplication.class, args);
    }

    // RestTemplate bean used by GroqService to make HTTP calls to the Groq API
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
