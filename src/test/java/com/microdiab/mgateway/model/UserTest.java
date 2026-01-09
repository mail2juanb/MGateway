package com.microdiab.mgateway.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import jakarta.validation.ConstraintViolation;

public class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertThat(user).isNotNull();
    }

    @Test
    void testParameterizedConstructor() {
        Long id = 1L;
        String username = "testUser";
        String password = "testPassword";
        String role = "USER";

        User user = new User(id, username, password, role);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();

        Long id = 1L;
        String username = "testUser";
        String password = "testPassword";
        String role = "USER";

        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    void testToString() {
        Long id = 1L;
        String username = "testUser";
        String password = "testPassword";
        String role = "USER";

        User user = new User(id, username, password, role);

        String expectedToString = "User{id=1, username='testUser', password='testPassword', role='USER'}";
        assertThat(user.toString()).isEqualTo(expectedToString);
    }

    @Test
    void testValidation_ValidUser() {
        User user = new User(1L, "testUser", "testPassword", "USER");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidation_InvalidUser_BlankUsername() {
        User user = new User(1L, "", "testPassword", "USER");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("username is mandatory");
    }

    @Test
    void testValidation_InvalidUser_BlankPassword() {
        User user = new User(1L, "testUser", "", "USER");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("password is mandatory");
    }
}
