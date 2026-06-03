package com.vocabverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VocabVerseApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabVerseApplication.class, args);
    }
}
