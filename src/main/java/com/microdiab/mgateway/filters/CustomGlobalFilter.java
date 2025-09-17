package com.microdiab.mgateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
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

        // Logique "pre"-filtre : ajout des en-têtes d'authentification
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(authentication -> {
                    // LOG DE LOGIN ICI
                    log.info("***** LOGIN SUCCESS dans le filtre *****");
                    log.info("Utilisateur authentifié : {} pour {}", authentication.getName(), path);
                    log.info("Rôles: {}", authentication.getAuthorities().toString());

                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .header("X-Auth-Username", authentication.getName())
                                .header("X-Auth-Roles", authentication.getAuthorities().toString())
                                .build();
                        ServerWebExchange newExchange = exchange.mutate().request(request).build();
                        log.info("*** Requête interceptée ! L'URL est : {}, User: {}, Roles: {}",
                                newExchange.getRequest().getURI(),
                                authentication.getName(),
                                authentication.getAuthorities());
                        return chain.filter(newExchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("*** Aucun utilisateur authentifié pour : {}", path);
                    log.info("*** Requête interceptée ! L'URL est : {}", exchange.getRequest().getURI());
                    return chain.filter(exchange);
                }))
                .doOnSuccess(aVoid -> {
                    // Logique "post"-filtre : après la réponse
                    ServerHttpResponse response = exchange.getResponse();
                    log.info("*** Réponse {} pour {}", response.getStatusCode(), path);
                    log.info("*** Réponse envoyée avec le statut : {}", response.getStatusCode());
                })
                .doOnError(throwable -> {
                    // Gestion des erreurs
                    log.error("*** Une erreur s'est produite : {}", throwable.getMessage());
                });
    }
}
