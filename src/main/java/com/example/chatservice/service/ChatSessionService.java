package com.example.chatservice.service;

import com.example.chatservice.api.dto.SessionDtos;
import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.repo.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

  private final ChatSessionRepository sessionRepo;

  @Transactional
  public ChatSession create(SessionDtos.CreateSessionRequest req) {
    var session = ChatSession.builder()
        .id(UUID.randomUUID())
        .userId(req.userId())
        .title(Optional.ofNullable(req.title()).filter(t -> !t.isBlank()).orElse("New Chat"))
        .favorite(false)
        .build();
    return sessionRepo.save(session);
  }

  @Transactional(readOnly = true)
  public Page<ChatSession> list(String userId, Boolean favorite, String q, int page, int size) {
    var pageable = PageRequest.of(page, Math.min(size, 200));
    if (q != null && !q.isBlank()) {
      return sessionRepo.findByUserIdAndTitleContainingIgnoreCase(userId, q, pageable);
    }
    if (favorite != null) {
      return sessionRepo.findByUserIdAndFavorite(userId, favorite, pageable);
    }
    return sessionRepo.findByUserId(userId, pageable);
  }

  @Transactional
  public ChatSession rename(UUID id, String title) {
    var s = sessionRepo.findById(id).orElseThrow(() -> new NotFoundException("session_not_found"));
    s.setTitle(title);
    return s;
  }

  @Transactional
  public ChatSession favorite(UUID id, boolean favorite) {
    var s = sessionRepo.findById(id).orElseThrow(() -> new NotFoundException("session_not_found"));
    s.setFavorite(favorite);
    return s;
  }

  @Transactional
  public void delete(UUID id) {
    if (!sessionRepo.existsById(id)) throw new NotFoundException("session_not_found");
    sessionRepo.deleteById(id);
  }
}
