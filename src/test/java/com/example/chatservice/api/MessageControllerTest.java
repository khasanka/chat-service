package com.example.chatservice.api;

import com.example.chatservice.domain.ChatMessage;
import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.service.ChatMessageService;
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

class MessageControllerTest {

    MockMvc mvc;

    ObjectMapper mapper = new ObjectMapper();

    @Mock
    ChatMessageService messageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(new MessageController(messageService)).build();
    }

    @Test
    void append_returnsCreated() throws Exception {
        UUID sid = UUID.randomUUID();
        var session = ChatSession.builder().id(sid).userId("u").title("t").favorite(false).build();
        var msg = ChatMessage.builder().id(UUID.randomUUID()).session(session).sender(ChatMessage.Sender.USER)
                .content("hello").build();
        when(messageService.append(any(), any())).thenReturn(msg);

        var req = new com.example.chatservice.api.dto.MessageDtos.CreateMessageRequest(ChatMessage.Sender.USER, "hello",
                null);
        mvc.perform(post("/api/v1/sessions/" + sid + "/messages").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(sid.toString()))
                .andExpect(jsonPath("$.content").value("hello"));
    }
}