package com.kaki.doctrack.authservice.service.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced  // Enables load balancing for the WebClient
    public WebClient webClient() {
        return WebClient.builder().build();  // Return a WebClient instance
    }
}
