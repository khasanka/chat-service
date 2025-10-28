package com.example.chatservice.security;

import com.example.chatservice.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {
  private final AppProperties props;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String path = request.getRequestURI();
    if (!path.startsWith("/api/")) {
      filterChain.doFilter(request, response);
      return;
    }
    String apiKey = request.getHeader("X-API-KEY");
    if (apiKey == null || apiKey.isBlank() || !isAllowedKey(apiKey)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"missing_or_invalid_api_key\"}");
      return;
    }
    var auth = new ApiKeyAuthenticationToken(apiKey, List.of(new SimpleGrantedAuthority("ROLE_API")));
    SecurityContextHolder.getContext().setAuthentication(auth);
    filterChain.doFilter(request, response);
  }

  private boolean isAllowedKey(String key) {
    Set<String> keys = Arrays.stream(props.getApiKeys().split(",")).map(String::trim).collect(Collectors.toSet());
    return keys.contains(key);
  }
}
