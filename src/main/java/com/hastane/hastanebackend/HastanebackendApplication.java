package com.hastane.hastanebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // YENİ IMPORT
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // YENİ IMPORT
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HastanebackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HastanebackendApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Hibernate 6 modülünü kaydet
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        objectMapper.registerModule(hibernate6Module);

        // Java 8 Date/Time (jsr310) modülünü kaydet
        objectMapper.registerModule(new JavaTimeModule()); // YENİ EKLENEN SATIR

        // Tarihlerin sayısal timestamp yerine ISO string formatında yazılmasını sağla
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // YENİ EKLENEN SATIR

        return objectMapper;
    }
}