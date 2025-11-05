package com.microdiab.mgateway.configuration;

import com.microdiab.mgateway.repository.UserRepository;
import com.microdiab.mgateway.service.CustomReactiveUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Ancienne version avec des utilisateurs en dur
//    @Bean
//    public MapReactiveUserDetailsService userDetailsService() {
//        UserDetails user = User.withUsername("user")
//                .password(passwordEncoder().encode("password"))
//                .roles("USER")
//                .build();
//        UserDetails adminUser = User.withUsername("admin")
//                .password(passwordEncoder().encode("admin"))
//                .roles("ADMIN")
//                .build();
//        return new MapReactiveUserDetailsService(user, adminUser);
//    }


//    @Bean
//    public ReactiveUserDetailsService userDetailsService() {
//        return new CustomReactiveUserDetailsService(userRepository);
//    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Desactive CSRF pour les APIs
                .authorizeExchange(exchanges ->
                        exchanges
                                //.pathMatchers("/clientui/home").permitAll()
                                //.pathMatchers("/clientui/login").permitAll()
                                .pathMatchers("/clientui/webjars/**").permitAll() // Ressources statiques accessibles sans authentification
                                //.pathMatchers("/webjars/**").permitAll() // Ressources statiques accessibles sans authentification
                                .pathMatchers("/clientui/css/**").permitAll()
                                .pathMatchers("/actuator/**").permitAll()
                                .pathMatchers("/logout", "/logout-success").permitAll() // Pages de logout non protegees
                                .anyExchange().authenticated() // Toutes les autres routes necessitent une authentification
                )
                .httpBasic(withDefaults()) // Active l'authentification basique
//                .formLogin(form -> form
//                                .loginPage("/clientui/login")  // Page de login servie par clientui
//                        // Pas de .loginProcessingUrl() en WebFlux !
//                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(customLogoutHandler()) // Gestionnaire personnalise
                        .logoutSuccessHandler(logoutSuccessHandler())
                );

        return http.build();
    }



    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        return (exchange, authentication) -> {
            System.out.println("***** LOGOUT SUCCESS HANDLER *****");
            System.out.println("Utilisateur complètement déconnecté");

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


    @Bean
    public ServerLogoutHandler customLogoutHandler() {
        return (exchange, authentication) -> {
            System.out.println("***** CUSTOM LOGOUT HANDLER *****");
            System.out.println("Invalidation complète pour: " +
                    (authentication != null ? authentication.getName() : "anonymous"));

            // Invalider explicitement la session
            return exchange.getExchange().getSession()
                    .doOnNext(session -> {
                        System.out.println("Session ID avant invalidation: " + session.getId());
                        session.getAttributes().clear(); // Vider tous les attributs
                        session.invalidate(); // Invalider la session
                        System.out.println("Session invalidée");
                    })
                    .then();
        };
    }
}
