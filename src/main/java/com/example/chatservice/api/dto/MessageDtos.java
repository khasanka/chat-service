package com.example.chatservice.api.dto;

import com.example.chatservice.domain.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class MessageDtos {
  public record CreateMessageRequest(
      @NotNull ChatMessage.Sender sender,
      @NotBlank @Size(max = 20000) String content,
      String contextJson
  ) {}

  public record MessageResponse(
      UUID id, UUID sessionId, ChatMessage.Sender sender,
      String content, String contextJson, Instant createdAt
  ) {}
}
