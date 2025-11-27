package com.kuspidsamples.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Global CORS configuration for the backend.
 *
 * - Supports dynamic frontend URL via environment variable FRONTEND_URL
 * - Falls back to localhost defaults for local development
 * - Configured for production readiness
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Resolve frontend origins from environment or fallback (useful for Docker / CI / Production)
        String frontendUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000");

        // Allowed origins (dynamic + common dev ports)
        config.setAllowedOrigins(Arrays.asList(
                frontendUrl,
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (authorization tokens, cookies)
        config.setAllowCredentials(true);

        // Exposed headers (client can read these)
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        // Preflight caching
        config.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
