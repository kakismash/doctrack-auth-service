package com.kaki.doctrack.authservice.security.filter;

import com.kaki.doctrack.authservice.exception.JWTException;
import com.kaki.doctrack.authservice.security.jwt.JwtUtil;
import com.kaki.doctrack.authservice.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReactiveAuthTokenFilter implements WebFilter {

    private final JwtUtil jwtUtils;

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ReactiveAuthTokenFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.justOrEmpty(parseJwt(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)))
                .filter(jwtUtils::validateJwtToken)
                .flatMap(jwt -> {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    return userService.findByUsername(username)
                            .map(this::createAuthenticationToken)
                            .flatMap(authentication -> {
                                SecurityContext context = new SecurityContextImpl(authentication);
                                return Mono.just(context)
                                        .flatMap(ctx -> chain.filter(exchange)
                                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx))));
                            });
                })
                .switchIfEmpty(chain.filter(exchange))
                .onErrorResume(e -> {
                    logger.error("Cannot set user authentication");
                    return handleAuthenticationError(exchange, e);
                });
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, Throwable e) {
        HttpStatus status;
        String errorMessage;

        if (e instanceof JwtException || e instanceof JWTException) {  // Adjust this based on your JWT exception type
            status = HttpStatus.UNAUTHORIZED;
            errorMessage = "Invalid or expired JWT token";
        } else if (e instanceof UsernameNotFoundException) { // Adjust based on your service exception type
            status = HttpStatus.FORBIDDEN;
            errorMessage = "User not found";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = "Internal server error";
        }

        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes())));
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private String parseJwt(String headerAuth) {
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

}
