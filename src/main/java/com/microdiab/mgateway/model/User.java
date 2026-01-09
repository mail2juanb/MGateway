package com.microdiab.mgateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a user entity stored in a MySQL database.
 * This class is used for user authentication and authorization within the microservice architecture.
 * It is mapped to the "users" table in the database and supports reactive data access via R2DBC.
 * User entity class.
 * The id field is auto-incremented by R2DBC if the corresponding column in the database is configured as such.
 */
@Table(name = "users")
public class User {

    /**
     * Unique identifier for the user.
     * Auto-incremented by the database.
     */
    @Id
    // R2DBC gère automatiquement l'auto-incrément si la colonne est définie comme telle en base
    private Long id;

    /**
     * Username of the user.
     * Must not be blank.
     */
    @NotBlank(message = "username is mandatory")
    private String username;

    /**
     * Password of the user.
     * Must not be blank.
     */
    @NotBlank(message = "password is mandatory")
    private String password;

    /**
     * Role of the user (e.g., "ADMIN", "USER", "INTERNAL").
     */
    private String role;


    /**
     * Gets the user's unique identifier.
     *
     * @return the user's id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's unique identifier.
     *
     * @param id the user's id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's username.
     *
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username the user's username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's password.
     *
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password the user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's role.
     *
     * @return the user's role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the user's role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id the user's unique identifier
     * @param username the user's username
     * @param password the user's password
     * @param role the user's role
     */
    public User(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }


    /**
     * Returns a string representation of the user.
     *
     * @return a string representation of the user
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
