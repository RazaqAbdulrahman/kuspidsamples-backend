package com.kuspidsamples.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Allow startup even if Cloudinary is not configured (for testing)
        // But log a warning
        if (cloudName == null || cloudName.isEmpty() || cloudName.isBlank()) {
            System.err.println("⚠️  WARNING: Cloudinary cloud-name is not configured!");
            System.err.println("⚠️  File upload features will not work.");
            System.err.println("⚠️  Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET");

            // Return a dummy Cloudinary instance to prevent startup failure
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dummy",
                    "api_key", "dummy",
                    "api_secret", "dummy",
                    "secure", true
            ));
        }

        if (apiKey == null || apiKey.isEmpty() || apiKey.isBlank()) {
            System.err.println("⚠️  WARNING: Cloudinary api-key is not configured!");
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dummy",
                    "api_key", "dummy",
                    "api_secret", "dummy",
                    "secure", true
            ));
        }

        if (apiSecret == null || apiSecret.isEmpty() || apiSecret.isBlank()) {
            System.err.println("⚠️  WARNING: Cloudinary api-secret is not configured!");
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dummy",
                    "api_key", "dummy",
                    "api_secret", "dummy",
                    "secure", true
            ));
        }

        System.out.println("✅ Cloudinary configuration loaded successfully");
        System.out.println("   Cloud Name: " + cloudName);

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}

// please work