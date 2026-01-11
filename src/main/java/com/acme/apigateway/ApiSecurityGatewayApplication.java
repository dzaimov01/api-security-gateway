package com.acme.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ApiSecurityGatewayApplication {
  public static void main(final String[] args) {
    SpringApplication.run(ApiSecurityGatewayApplication.class, args);
  }
}
