package com.example.chatservice.api;

import com.example.chatservice.api.dto.MessageDtos;
import com.example.chatservice.api.dto.PageResponse;
import com.example.chatservice.domain.ChatMessage;
import com.example.chatservice.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
public class MessageController {

  private final ChatMessageService messageService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MessageDtos.MessageResponse append(@PathVariable UUID sessionId, @Valid @RequestBody MessageDtos.CreateMessageRequest req) {
    var m = messageService.append(sessionId, req);
    return map(m);
  }

  @GetMapping
  public PageResponse<MessageDtos.MessageResponse> history(
      @PathVariable UUID sessionId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size
  ) {
    Page<ChatMessage> p = messageService.history(sessionId, page, size);
    return PageResponse.from(p.map(this::map));
  }

  private MessageDtos.MessageResponse map(ChatMessage m) {
    return new MessageDtos.MessageResponse(m.getId(), m.getSession().getId(), m.getSender(), m.getContent(), m.getContextJson(), m.getCreatedAt());
  }
}
