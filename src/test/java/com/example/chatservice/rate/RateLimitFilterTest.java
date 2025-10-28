package com.example.chatservice.rate;

import com.example.chatservice.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

  @Test
  void rateLimit_blocksAfterCapacity() throws Exception {
    AppProperties props = Mockito.mock(AppProperties.class);
    AppProperties.RateLimit rl = new AppProperties.RateLimit();
    rl.setCapacity(1);
    rl.setRefillPerMinute(60);
    Mockito.when(props.getRateLimit()).thenReturn(rl);

    RateLimitFilter f = new RateLimitFilter(props);

    var req1 = new MockHttpServletRequest("GET", "/api/v1/sessions");
    req1.addHeader("X-API-KEY", "k");
    var res1 = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    f.doFilter(req1, res1, chain);
    assertEquals(200, res1.getStatus());

    var req2 = new MockHttpServletRequest("GET", "/api/v1/sessions");
    req2.addHeader("X-API-KEY", "k");
    var res2 = new MockHttpServletResponse();

    f.doFilter(req2, res2, chain);
    assertEquals(429, res2.getStatus());
    assertEquals("rate_limited", res2.getErrorMessage());
  }
}
