package com.microdiab.MGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MGatewayApplication.class, args);
    }

}
