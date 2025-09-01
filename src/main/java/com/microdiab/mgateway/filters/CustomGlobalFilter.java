package com.microdiab.mgateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class CustomGlobalFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Logique "pre"-filtre : exécutée avant que la requête ne soit envoyée au service en aval
        ServerHttpRequest request = exchange.getRequest();
        log.info("*** Requête interceptée ! L'URL est : {}", request.getURI());

        // Appel pour continuer la chaîne de filtres
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    // Logique "post"-filtre : exécutée après que la réponse a été reçue du service en aval
                    ServerHttpResponse response = exchange.getResponse();
                    log.info("*** Réponse envoyée avec le statut : {}", response.getStatusCode());
                })
                .doOnError(throwable -> {
                    // Gestion des erreurs
                    log.error("Une erreur s'est produite : {}", throwable.getMessage());
                });
    }
}
