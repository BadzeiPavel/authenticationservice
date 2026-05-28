package com.innowise.authenticationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@ConfigurationPropertiesScan
@SpringBootApplication
public class AuthenticationserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthenticationserviceApplication.class, args);
  }

}
