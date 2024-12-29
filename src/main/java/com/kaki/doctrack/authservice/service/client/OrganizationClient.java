package com.kaki.doctrack.authservice.service.client;

import com.kaki.doctrack.authservice.dto.client.OrganizationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrganizationClient {

    @Value("${spring.application.name}")
    private String organizationServiceName;

    @Value("${api.internal.header}")
    private String internalHeader;

    @Value("${api.internal.apiGatewayPathUrl}")
    private String apiGatewayPathUrl;

    @Value("${api.internal.organizationServicePath}")
    private String organizationServiceUrl;

    @Value("${api.internal.apiKey}")
    private String apiKey;

    private final Logger logger = LoggerFactory.getLogger(OrganizationClient.class);

    private final WebClient webClient;

    public OrganizationClient(
            WebClient.Builder webClientBuilder,
            @Value("${api.internal.apiGatewayPathUrl}") String apiGatewayPathUrl,
            @Value("${api.internal.organizationServicePath}") String organizationServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(apiGatewayPathUrl.concat(organizationServiceUrl)).build();
    }

    public Mono<OrganizationDto> getOrganizationById(Long organizationId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{id}")
                        .build(organizationId))
                .header(internalHeader, organizationServiceName)
                .header("X-Internal-Api-Key", apiKey)
                .retrieve()
                .bodyToMono(OrganizationDto.class);
    }

    public Flux<OrganizationDto> getOrganizationsByIds(Mono<Set<Long>> organizationIds) {
        return organizationIds
                .map(ids -> ids.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .flatMapMany(idsParam ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/batch")
                                        .queryParam("ids", idsParam)
                                        .build())
                                .header(internalHeader, organizationServiceName)
                                .header("X-Internal-Api-Key", apiKey)
                                .retrieve()
                                .bodyToFlux(OrganizationDto.class)
                );
    }
}
