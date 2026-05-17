package com.example.r2dbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
public class R2dbcJwtAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(R2dbcJwtAuthApplication.class, args);
    }
}
