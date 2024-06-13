package com.kaki.doctrack.authservice.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> log.info("Successfully processed request: {}", exchange.getRequest().getURI()))
                .doOnError(throwable -> log.error("Error processing request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI(), throwable));
    }
}
