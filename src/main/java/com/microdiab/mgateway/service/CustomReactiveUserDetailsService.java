package com.microdiab.mgateway.service;

import com.microdiab.mgateway.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Custom implementation of {@link ReactiveUserDetailsService} for loading user details reactively.
 * Uses the {@link UserRepository} to fetch user data and maps it to Spring Security's {@link UserDetails}.
 * Implements {@link ReactiveUserDetailsService} to provide reactive user details for authentication.
 */
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    /** Repository for accessing user data. */
    private final UserRepository userRepository;

    /**
     * Constructor for CustomReactiveUserDetailsService.
     *
     * @param userRepository the user repository
     */
    public CustomReactiveUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by their username and maps it to Spring Security's {@link UserDetails}.
     *
     * @param username the username to search for
     * @return a {@link Mono} emitting the user details if found
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found : " + username)))
                .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build());
    }
}
