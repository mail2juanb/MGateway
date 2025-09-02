package com.microdiab.mgateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain (ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Désactive CSRF pour les APIs
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/clientui/webjars/**").permitAll() // Exemple : ressources statiques accessibles sans auth
                                .pathMatchers("/actuator/**").permitAll()
                                .anyExchange().authenticated() // Toutes les autres routes nécessitent une authentification
                )
                .httpBasic(withDefaults()); // Active l'authentification basique

        return http.build();
    }

}
