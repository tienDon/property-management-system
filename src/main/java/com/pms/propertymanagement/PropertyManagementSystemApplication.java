package com.pms.propertymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Enable scheduled job support for subscription expiration
public class PropertyManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyManagementSystemApplication.class, args);
    }

}
