package com.visiblethread.documentanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DocumentAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentAnalyzerApplication.class, args);
    }

}
