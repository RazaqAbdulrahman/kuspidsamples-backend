package com.kuspidsamples.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestCloudinaryConfig {

    @Bean
    @Primary
    public Cloudinary cloudinary() {
        // Return a mock Cloudinary instance for tests
        return mock(Cloudinary.class);
    }
}