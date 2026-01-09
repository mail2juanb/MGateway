package com.microdiab.mgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the MGateway microservice.
 * Enables service discovery with Eureka and starts the Spring Boot application.
 * Entry point for the MGateway microservice.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MgatewayApplication {

    /**
     * Main method to start the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MgatewayApplication.class, args);
    }

}
