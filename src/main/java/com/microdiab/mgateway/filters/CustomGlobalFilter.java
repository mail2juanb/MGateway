package com.microdiab.mgateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class CustomGlobalFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();
        String path = originalRequest.getURI().getPath();
        String method = originalRequest.getMethod().toString();

        log.info("*** FILTRE - {} {}", method, path);

        // Vérifie si les headers d'authentification existent déjà
        if (!originalRequest.getHeaders().containsKey("X-Auth-Username")) {
            log.info("*** Vérifie si les headers d'authentification existent déjà");
            return ReactiveSecurityContextHolder.getContext()
                    .flatMap(securityContext -> {
                        Authentication authentication = securityContext.getAuthentication();
                        if (authentication != null && authentication.isAuthenticated()) {
                            log.info("***** LOGIN SUCCESS dans le filtre *****");
                            log.info("Utilisateur authentifié : {} pour {}", authentication.getName(), path);
                            log.info("Rôles: {}", authentication.getAuthorities().toString());

                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-Auth-Username", authentication.getName())
                                    .header("X-Auth-Roles", authentication.getAuthorities().toString())
                                    .build();
                            ServerWebExchange newExchange = exchange.mutate().request(request).build();
                            return chain.filter(newExchange);
                        } else {
                            log.info("*** Aucun utilisateur authentifié pour : {}", path);
                            return chain.filter(exchange);
                        }
                    });
        } else {
            // Les headers existent déjà, on passe la requête sans modification
            log.info("*** Les headers existent déjà, on passe la requête sans modification");
            return chain.filter(exchange);
        }}
}
