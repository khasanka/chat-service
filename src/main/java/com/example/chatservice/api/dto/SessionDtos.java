package com.example.chatservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public class SessionDtos {
  public record CreateSessionRequest(
      @NotBlank @Size(max=128) String userId,
      @Size(max=255) String title
  ) {}

  public record RenameSessionRequest(
      @NotBlank @Size(max=255) String title
  ) {}

  public record FavoriteRequest(boolean favorite) {}

  public record SessionResponse(
      UUID id, String userId, String title, boolean favorite,
      Instant createdAt, Instant updatedAt
  ) {}
}
