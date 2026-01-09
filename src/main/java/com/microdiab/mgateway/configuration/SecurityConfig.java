package com.microdiab.mgateway.configuration;

import com.microdiab.mgateway.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuration class for security settings in the MGateway microservice.
 * This class defines the security filter chain, password encoding, and logout handling
 * for a reactive Spring WebFlux application.
 *
 * <p>It uses Spring Security's {@link EnableWebFluxSecurity} to enable security for WebFlux applications.
 * The configuration includes:
 * <ul>
 *   <li>Disabling CSRF protection for API endpoints.</li>
 *   <li>Permitting public access to static resources, actuator endpoints, and logout URLs.</li>
 *   <li>Enforcing authentication for all other endpoints.</li>
 *   <li>Configuring HTTP Basic authentication.</li>
 *   <li>Custom logout handlers to invalidate sessions and clear cache.</li>
 * </ul>
 *
 * @see EnableWebFluxSecurity
 * @see ServerHttpSecurity
 * @see SecurityWebFilterChain
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    /**
     * Constructs a new SecurityConfig with the specified UserRepository.
     *
     * @param userRepository the repository used to access user data
     */
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Provides a BCrypt password encoder bean for encoding user passwords.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for the application.
     * Defines public and private endpoints, authentication method, and logout behavior.
     *
     * @param http the ServerHttpSecurity instance to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/clientui/webjars/**").permitAll() // Ressources statiques accessibles sans authentification
                                .pathMatchers("/clientui/css/**").permitAll()
                                .pathMatchers(
                                        "/actuator/**",
                                        "/apidocs/**",
                                        "/swagger*/**",
                                        "/v3/api-docs/**",
                                        "/webjars/**",
                                        "/favicon.ico"
                                ).permitAll()
                                .pathMatchers("/logout", "/logout-success").permitAll() // Pages de logout non protegees
                                .anyExchange().authenticated() // Toutes les autres routes necessitent une authentification
                )
                .httpBasic(withDefaults()) // Active l'authentification basique
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Desactive CSRF pour les APIs
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(customLogoutHandler()) // Gestionnaire personnalise
                        .logoutSuccessHandler(logoutSuccessHandler())
                );

        return http.build();
    }

    /**
     * Provides a custom logout success handler.
     * Redirects to the home page and clears cache/cookies after logout.
     *
     * @return a ServerLogoutSuccessHandler instance
     */
    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        return (exchange, authentication) -> {

            ServerHttpResponse response = exchange.getExchange().getResponse();
            response.setStatusCode(HttpStatus.FOUND);
            response.getHeaders().add("Location", "/clientui/home");

            // Headers pour forcer la suppression du cache
            response.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            response.getHeaders().add("Pragma", "no-cache");
            response.getHeaders().add("Expires", "0");
            response.getHeaders().add("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"");

            return response.setComplete();
        };
    }

    /**
     * Provides a custom logout handler to invalidate the user session.
     *
     * @return a ServerLogoutHandler instance
     */
    @Bean
    public ServerLogoutHandler customLogoutHandler() {
        return (exchange, authentication) -> {
            // Invalider explicitement la session
            return exchange.getExchange().getSession()
                    .doOnNext(session -> {
                        session.getAttributes().clear(); // Vider tous les attributs
                        session.invalidate(); // Invalider la session
                    })
                    .then();
        };
    }
}
