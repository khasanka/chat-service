package com.example.chatservice.security;

import com.example.chatservice.config.AppProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final AppProperties props;
  private final ApiKeyAuthFilter apiKeyAuthFilter;
  private final Environment env;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> {
            // health and basic endpoints are public
            auth.requestMatchers("/actuator/health/**", "/actuator/info").permitAll();
            // in dev we allow fetching the generated OpenAPI at runtime (useful for CI/dev regeneration)
            if (env.acceptsProfiles(Profiles.of("dev"))) {
              auth.requestMatchers("/openapi.json", "/docs/**", "/v3/api-docs/**").permitAll();
            } else {
              // static OpenAPI spec and docs should require authentication (API key) in non-dev
              auth.requestMatchers("/openapi.json", "/docs/**", "/v3/api-docs/**").authenticated();
            }
            // block runtime swagger UI endpoints (we serve ReDoc at /docs/)
            auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html").denyAll();
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            auth.requestMatchers("/error").permitAll();
            auth.requestMatchers("/h2/**", "/h2-console/**", "/h2-console").permitAll();
            auth.requestMatchers("/api/**").authenticated();
            auth.anyRequest().denyAll();
        })
        .headers(h -> h.frameOptions(f -> f.sameOrigin()));
    // API key filter will populate authentication for requests bearing X-API-KEY
    http.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var cfg = new CorsConfiguration();
    var origins = Arrays.stream(props.getCors().getAllowedOrigins().split(",")).map(String::trim).toList();
    cfg.setAllowedOrigins(origins);
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("Content-Type", "X-API-KEY", "Authorization"));
    cfg.setExposedHeaders(List.of("X-RateLimit-Remaining", "Retry-After"));
    cfg.setAllowCredentials(false);
    var src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", cfg);
    return src;
  }
}
