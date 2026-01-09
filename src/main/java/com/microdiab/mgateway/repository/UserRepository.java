package com.microdiab.mgateway.repository;

import com.microdiab.mgateway.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository interface for managing {@link User} entities.
 * Provides reactive CRUD operations and a custom method to find a user by their username.
 * Extends {@link ReactiveCrudRepository} to support reactive programming.
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return a {@link Mono} emitting the user if found, or empty if not found
     */
    Mono<User> findByUsername(String username);
}
