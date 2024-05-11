package io.seoleir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParserApp {
    public static void main(String[] args) {
        SpringApplication.run(ParserApp.class, args);
    }
}