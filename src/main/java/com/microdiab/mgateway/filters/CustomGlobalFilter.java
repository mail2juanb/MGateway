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

/**
 * Global filter for the MGateway microservice.
 * This filter passes before all others.
 * This filter intercepts all incoming requests and adds authentication headers
 * (X-Auth-Username and X-Auth-Roles) if the user is authenticated.
 *
 * <p>It logs request details and authentication status for debugging and monitoring purposes.
 *
 * @see GlobalFilter
 * @see ReactiveSecurityContextHolder
 */
@Component
@Order(-1)
public class CustomGlobalFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomGlobalFilter.class);

    /**
     * Filters incoming requests to add authentication headers if the user is authenticated.
     * Logs request method, path, and authentication details for debugging.
     *
     * @param exchange the current ServerWebExchange
     * @param chain the GatewayFilterChain to continue processing the request
     * @return a Mono indicating completion of the filter processing
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();
        String path = originalRequest.getURI().getPath();
        String method = originalRequest.getMethod().toString();

        log.debug("*** FILTER - {} {}", method, path);

        // Vérifie si les headers d'authentification existent déjà
        if (!originalRequest.getHeaders().containsKey("X-Auth-Username")) {
            //log.debug("*** Vérifie si les headers d'authentification existent déjà");
            return ReactiveSecurityContextHolder.getContext()
                    .flatMap(securityContext -> {
                        Authentication authentication = securityContext.getAuthentication();
                        if (authentication != null && authentication.isAuthenticated()) {
                            log.debug("***** LOGIN SUCCESS in filter *****");
                            log.debug("Authenticated user: {} for {}", authentication.getName(), path);
                            log.debug("Rôles: {}", authentication.getAuthorities().toString());

                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-Auth-Username", authentication.getName())
                                    .header("X-Auth-Roles", authentication.getAuthorities().toString())
                                    .build();
                            ServerWebExchange newExchange = exchange.mutate().request(request).build();
                            return chain.filter(newExchange);
                        } else {
                            log.debug("*** No authenticated users for: {}", path);
                            return chain.filter(exchange);
                        }
                    });
        } else {
            // Les headers existent déjà, on passe la requête sans modification
            log.debug("*** The headers already exist, so we pass the request without modification.");
            return chain.filter(exchange);
        }}
}
