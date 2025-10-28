package com.example.chatservice.service;

import com.example.chatservice.api.dto.MessageDtos;
import com.example.chatservice.domain.ChatMessage;
import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.repo.ChatMessageRepository;
import com.example.chatservice.repo.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

  private final ChatMessageRepository messageRepo;
  private final ChatSessionRepository sessionRepo;

  @Transactional
  public ChatMessage append(UUID sessionId, MessageDtos.CreateMessageRequest req) {
    ChatSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new NotFoundException("session_not_found"));
    ChatMessage msg = ChatMessage.builder()
        .id(UUID.randomUUID())
        .session(session)
        .sender(req.sender())
        .content(req.content())
        .contextJson(req.contextJson())
        .build();
    return messageRepo.save(msg);
  }

  @Transactional(readOnly = true)
  public Page<ChatMessage> history(UUID sessionId, int page, int size) {
    ChatSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new NotFoundException("session_not_found"));
    return messageRepo.findBySessionOrderByCreatedAtAsc(session, PageRequest.of(page, Math.min(size, 200)));
  }
}
