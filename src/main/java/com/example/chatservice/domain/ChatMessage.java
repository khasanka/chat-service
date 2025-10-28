package com.example.chatservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_message_session", columnList = "session_id, created_at")
})

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
  @Id
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false, columnDefinition = "uuid")
  private ChatSession session;

  @Enumerated(EnumType.STRING)
  @Column(name = "sender", nullable = false, length = 20)
  private Sender sender;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "context_json", columnDefinition = "TEXT")
  private String contextJson;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public enum Sender { USER, ASSISTANT, SYSTEM }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ChatSession getSession() {
    return session;
  }

  public void setSession(ChatSession session) {
    this.session = session;
  }

  public Sender getSender() {
    return sender;
  }

  public void setSender(Sender sender) {
    this.sender = sender;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContextJson() {
    return contextJson;
  }

  public void setContextJson(String contextJson) {
    this.contextJson = contextJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
