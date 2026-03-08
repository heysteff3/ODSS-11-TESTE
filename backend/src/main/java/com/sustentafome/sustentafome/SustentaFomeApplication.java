package com.sustentafome.sustentafome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SustentaFomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SustentaFomeApplication.class, args);
    }
}
