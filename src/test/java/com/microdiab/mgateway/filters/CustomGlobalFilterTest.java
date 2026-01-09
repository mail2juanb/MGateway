package com.microdiab.mgateway.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomGlobalFilterTest {

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private CustomGlobalFilter customGlobalFilter;

    private ServerWebExchange exchange;
    private ServerHttpRequest request;

    @BeforeEach
    void setUp() {
        request = MockServerHttpRequest.method(HttpMethod.GET, "/test").build();
        exchange = MockServerWebExchange.builder((MockServerHttpRequest) request).build();
    }

    @Test
    void filter_WhenNoAuthHeadersAndUserAuthenticated_ShouldAddHeaders() {
        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mock GatewayFilterChain
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Utilise thenAnswer pour retourner une Collection<GrantedAuthority>
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> {
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    return authorities;
                });

        // Mock ReactiveSecurityContextHolder.getContext() avec mockStatic
        try (MockedStatic<ReactiveSecurityContextHolder> mockedStatic = Mockito.mockStatic(ReactiveSecurityContextHolder.class)) {
            mockedStatic.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.just(securityContext));

            // Exécution
            Mono<Void> result = customGlobalFilter.filter(exchange, chain);

            // Vérification
            StepVerifier.create(result)
                    .verifyComplete();
        }

        // Vérifie que la chaîne est appelée avec un nouvel échange
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }




    @Test
    void filter_WhenNoAuthHeadersAndUserNotAuthenticated_ShouldNotAddHeaders() {
        // Mock SecurityContext sans Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Mock GatewayFilterChain
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Mock ReactiveSecurityContextHolder.getContext() avec mockStatic
        try (MockedStatic<ReactiveSecurityContextHolder> mockedStatic = Mockito.mockStatic(ReactiveSecurityContextHolder.class)) {
            mockedStatic.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.just(securityContext));

            // Exécution
            Mono<Void> result = customGlobalFilter.filter(exchange, chain);

            // Vérification
            StepVerifier.create(result)
                    .verifyComplete();
        }

        // Vérifie que la chaîne est appelée sans modification
        verify(chain, times(1)).filter(exchange);
    }



    @Test
    void filter_WhenAuthHeadersExist_ShouldNotModifyRequest() {
        request = MockServerHttpRequest.method(HttpMethod.GET, "/test")
                .header("X-Auth-Username", "existingUser")
                .build();
        exchange = MockServerWebExchange.builder((MockServerHttpRequest) request).build();

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = customGlobalFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_WhenNoAuthHeadersAndUserNotAuthenticatedButNotNull_ShouldNotAddHeaders() {
        // Mock Authentication (non-authentifié)
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mock GatewayFilterChain
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Mock ReactiveSecurityContextHolder.getContext() avec mockStatic
        try (MockedStatic<ReactiveSecurityContextHolder> mockedStatic = Mockito.mockStatic(ReactiveSecurityContextHolder.class)) {
            mockedStatic.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.just(securityContext));

            // Exécution
            Mono<Void> result = customGlobalFilter.filter(exchange, chain);

            // Vérification
            StepVerifier.create(result)
                    .verifyComplete();
        }

        // Vérifie que la chaîne est appelée sans modification
        verify(chain, times(1)).filter(exchange);
    }
}
