package com.example.chatservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

// Spring automatically registers our OncePerRequestFilters; order is determined by @Order
@Configuration
@RequiredArgsConstructor
class FilterOrderingConfig {
}
