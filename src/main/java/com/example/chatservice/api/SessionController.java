package com.example.chatservice.api;

import com.example.chatservice.api.dto.PageResponse;
import com.example.chatservice.api.dto.SessionDtos;
import com.example.chatservice.domain.ChatSession;
import com.example.chatservice.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

  private final ChatSessionService sessionService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SessionDtos.SessionResponse create(@Valid @RequestBody SessionDtos.CreateSessionRequest req) {
    var s = sessionService.create(req);
    return map(s);
  }

  @GetMapping
  public PageResponse<SessionDtos.SessionResponse> list(
      @RequestParam String userId,
      @RequestParam(required = false) Boolean favorite,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Page<ChatSession> p = sessionService.list(userId, favorite, q, page, size);
    return new PageResponse<>(p.map(this::map).getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }

  @PatchMapping("/{id}/rename")
  public SessionDtos.SessionResponse rename(@PathVariable UUID id, @Valid @RequestBody SessionDtos.RenameSessionRequest req) {
    return map(sessionService.rename(id, req.title()));
  }

  @PutMapping("/{id}/favorite")
  public SessionDtos.SessionResponse favorite(@PathVariable UUID id, @Valid @RequestBody SessionDtos.FavoriteRequest req) {
    return map(sessionService.favorite(id, req.favorite()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    sessionService.delete(id);
  }

  private SessionDtos.SessionResponse map(ChatSession s) {
    return new SessionDtos.SessionResponse(s.getId(), s.getUserId(), s.getTitle(), s.isFavorite(), s.getCreatedAt(), s.getUpdatedAt());
  }
}
