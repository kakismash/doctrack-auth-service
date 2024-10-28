package com.kaki.doctrack.authservice.rest;

import com.kaki.doctrack.authservice.service.InternalApiKeyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.path}${api.version}/internal-api-key")
@RequiredArgsConstructor
public class InternalApiKeyController {

    private final Logger logger = LoggerFactory.getLogger(InternalApiKeyController.class);

    private final InternalApiKeyService internalApiKeyService;

    @GetMapping("/generate/{microserviceName}")
    public Mono<ResponseEntity<String>> generateApiKey(
            @PathVariable String microserviceName
    ) {

        logger.info("Generating API key for microservice: {}", microserviceName);

        return internalApiKeyService.generateApiKey(microserviceName)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/validate/{microserviceName}")
    public Mono<ResponseEntity<String>> decodeApiKey(
            @PathVariable String microserviceName,
            @RequestHeader("X-Internal-Api-Key") String apiKey
    ) {

        logger.info("Validating API key for microservice: {}", microserviceName);

        return internalApiKeyService.decodeApiKey(apiKey)
                .map(ResponseEntity::ok);
    }
}
