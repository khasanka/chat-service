package com.example.chatservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_session", indexes = {
    @Index(name = "idx_session_user", columnList = "user_id"),
    @Index(name = "idx_session_title", columnList = "title")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
  @Id
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", nullable = false, length = 128)
  private String userId;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "favorite", nullable = false)
  private boolean favorite;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChatMessage> messages = new ArrayList<>();
}
