package com.example.chatservice.api;

import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessionControllerTest {

    MockMvc mvc;

    ObjectMapper mapper = new ObjectMapper();

    @Mock
    ChatSessionService sessionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(new SessionController(sessionService)).build();
    }

    @Test
    void create_returnsCreated() throws Exception {
        var s = ChatSession.builder().id(UUID.randomUUID()).userId("u").title("t").favorite(false).build();
        when(sessionService.create(any())).thenReturn(s);

        var req = new com.example.chatservice.api.dto.SessionDtos.CreateSessionRequest("u", "t");
        mvc.perform(post("/api/v1/sessions").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u"))
                .andExpect(jsonPath("$.title").value("t"));
    }
}
