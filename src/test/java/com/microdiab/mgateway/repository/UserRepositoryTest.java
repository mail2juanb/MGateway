package com.microdiab.mgateway.repository;

import com.microdiab.mgateway.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void findByUsername_shouldReturnUser() {
        // Arrange
        User expectedUser = new User(1L, "testuser", "password", "ROLE_USER");
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Mono.just(expectedUser));

        // Act & Assert
        StepVerifier.create(userRepository.findByUsername("testuser"))
                .expectNext(expectedUser)
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldReturnEmptyMono_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userRepository.findByUsername("unknownuser"))
                .verifyComplete();
    }
}
