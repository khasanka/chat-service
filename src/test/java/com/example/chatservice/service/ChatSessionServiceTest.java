package com.example.chatservice.service;

import com.example.chatservice.api.dto.SessionDtos;
import com.example.chatservice.domain.ChatSession;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    ChatSessionRepository sessionRepo;

    @InjectMocks
    ChatSessionService service;

    @Test
    void create_shouldSaveAndReturnSession() {
        when(sessionRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new SessionDtos.CreateSessionRequest("alice", "My chat");
        ChatSession s = service.create(req);

        assertThat(s.getUserId()).isEqualTo("alice");
        assertThat(s.getTitle()).isEqualTo("My chat");
        assertThat(s.isFavorite()).isFalse();
        assertThat(s.getId()).isNotNull();
        verify(sessionRepo).save(any());
    }

    @Test
    void list_callsRepository() {
        var page = new PageImpl<>(
                List.of(ChatSession.builder().id(UUID.randomUUID()).userId("u").title("t").favorite(false).build()));
        when(sessionRepo.findByUserId(any(), any())).thenReturn(page);

        var p = service.list("u", null, null, 0, 10);
        assertThat(p.getTotalElements()).isEqualTo(1);
        verify(sessionRepo).findByUserId(any(), any());
    }

    @Test
    void rename_whenNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(sessionRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.rename(id, "new title"));
    }

    @Test
    void favorite_toggle_andRename_updateFields() {
        UUID id = UUID.randomUUID();
        var s = ChatSession.builder().id(id).userId("u").title("t").favorite(false).build();
        when(sessionRepo.findById(id)).thenReturn(Optional.of(s));

        var renamed = service.rename(id, "new");
        assertThat(renamed.getTitle()).isEqualTo("new");

        var favored = service.favorite(id, true);
        assertThat(favored.isFavorite()).isTrue();
    }

    @Test
    void delete_whenNotExists_throws() {
        UUID id = UUID.randomUUID();
        when(sessionRepo.existsById(id)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.delete(id));
    }
}
