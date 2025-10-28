package com.example.chatservice.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
  private final String principal;

  ApiKeyAuthenticationToken(String apiKey, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = apiKey;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return "";
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
