package com.dong.judge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableConfigurationProperties
@EnableMongoRepositories
public class JudgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JudgeApplication.class, args);
    }

}
