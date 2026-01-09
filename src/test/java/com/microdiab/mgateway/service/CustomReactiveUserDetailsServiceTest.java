package com.microdiab.mgateway.service;

import com.microdiab.mgateway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomReactiveUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomReactiveUserDetailsService userDetailsService;

    @Test
    void findByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        var user = new com.microdiab.mgateway.model.User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Mono.just(user));

        // Act & Assert
        StepVerifier.create(userDetailsService.findByUsername("testuser"))
                .expectNextMatches(userDetails ->
                        userDetails.getUsername().equals("testuser") &&
                                userDetails.getPassword().equals("password") &&
                                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
                )
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldThrowException_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userDetailsService.findByUsername("unknownuser"))
                .expectErrorMatches(throwable ->
                        throwable instanceof UsernameNotFoundException &&
                                throwable.getMessage().equals("User not found : unknownuser")
                )
                .verify();
    }
}
