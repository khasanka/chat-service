package com.example.chatservice.config;

import lombok.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private String apiKeys = "dev-key";
    private Cors cors = new Cors();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }

    @Data
    public static class RateLimit {
        private int capacity = 60;
        private int refillPerMinute = 60;
    }
}