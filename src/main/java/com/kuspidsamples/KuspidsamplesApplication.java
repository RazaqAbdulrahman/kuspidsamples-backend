package com.kuspidsamples;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KuspidsamplesApplication {

    public static void main(String[] args) {
        // Load environment variables from .env file
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't fail if .env is missing (for production)
                    .load();

            // Set system properties from .env
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            System.out.println("✅ Environment variables loaded successfully");
        } catch (Exception e) {
            System.out.println("⚠️  No .env file found, using system environment variables");
        }

        // Start Spring Boot application
        SpringApplication.run(KuspidsamplesApplication.class, args);
    }
}