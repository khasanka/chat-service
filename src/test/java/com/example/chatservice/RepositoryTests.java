package com.example.chatservice;

import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.repo.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoryTests {

  @Autowired
  ChatSessionRepository sessionRepo;

  @Test
  void createSession() {
    var s = ChatSession.builder()
        .id(UUID.randomUUID())
        .userId("u1")
        .title("T1")
        .favorite(false)
        .build();
    sessionRepo.save(s);
    assertThat(sessionRepo.findById(s.getId())).isPresent();
  }
}
