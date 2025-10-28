package com.example.chatservice.service;

import com.example.chatservice.api.dto.MessageDtos;
import com.example.chatservice.domain.ChatMessage;
import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.repo.ChatMessageRepository;
import com.example.chatservice.repo.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

  @Mock
  ChatMessageRepository messageRepo;

  @Mock
  ChatSessionRepository sessionRepo;

  @InjectMocks
  ChatMessageService service;

  @Test
  void append_shouldSaveMessage() {
    UUID sid = UUID.randomUUID();
    var session = ChatSession.builder().id(sid).userId("u").title("t").favorite(false).build();
    when(sessionRepo.findById(sid)).thenReturn(Optional.of(session));
    when(messageRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    var req = new MessageDtos.CreateMessageRequest(ChatMessage.Sender.USER, "hello", null);
    ChatMessage m = service.append(sid, req);

    assertThat(m.getSession().getId()).isEqualTo(sid);
    assertThat(m.getContent()).isEqualTo("hello");
    assertThat(m.getSender()).isEqualTo(ChatMessage.Sender.USER);
  }

  @Test
  void append_whenSessionMissing_throws() {
    UUID sid = UUID.randomUUID();
    when(sessionRepo.findById(sid)).thenReturn(Optional.empty());
    var req = new MessageDtos.CreateMessageRequest(ChatMessage.Sender.USER, "hi", null);
    assertThrows(NotFoundException.class, () -> service.append(sid, req));
  }

  @Test
  void history_returnsPage() {
    UUID sid = UUID.randomUUID();
    var session = ChatSession.builder().id(sid).userId("u").title("t").favorite(false).build();
    when(sessionRepo.findById(sid)).thenReturn(Optional.of(session));
    var msg = ChatMessage.builder().id(UUID.randomUUID()).session(session).sender(ChatMessage.Sender.USER).content("c").build();
    when(messageRepo.findBySessionOrderByCreatedAtAsc(any(), any())).thenReturn(new PageImpl<>(List.of(msg)));

    var p = service.history(sid, 0, 10);
    assertThat(p.getTotalElements()).isEqualTo(1);
  }
}
