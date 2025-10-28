package com.example.chatservice.security;

import com.example.chatservice.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiKeyAuthFilterTest {

  @Test
  void missingApiKey_returns401() throws Exception {
    AppProperties props = Mockito.mock(AppProperties.class);
    Mockito.when(props.getApiKeys()).thenReturn("a,b");
    ApiKeyAuthFilter f = new ApiKeyAuthFilter(props);

    var req = new MockHttpServletRequest("GET", "/api/v1/sessions");
    var res = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    f.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    assertEquals("{\"error\":\"missing_or_invalid_api_key\"}", res.getContentAsString());
  }

  @Test
  void validApiKey_allowsChain() throws Exception {
    AppProperties props = Mockito.mock(AppProperties.class);
    Mockito.when(props.getApiKeys()).thenReturn("a,b");
    ApiKeyAuthFilter f = new ApiKeyAuthFilter(props);

    var req = new MockHttpServletRequest("GET", "/api/v1/sessions");
    req.addHeader("X-API-KEY", "a");
    var res = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    f.doFilter(req, res, chain);

    // chain should proceed (default status = 200)
    assertEquals(200, res.getStatus());
  }
}
