package ru.cinema.service.playlist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cinema.dto.playlist.AddPlaylistItemRequest;
import ru.cinema.dto.playlist.CreatePlaylistRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.model.Movie;
import ru.cinema.model.Playlist;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.PlaylistRepository;
import ru.cinema.service.user.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link PlaylistService}: create, addItem (conflict), getVisible (forbidden).
 */
@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock PlaylistRepository playlistRepo;
    @Mock ContentRepository contentRepo;
    @Mock UserService userService;

    @InjectMocks PlaylistService service;

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("u" + id);
        u.setRole(UserRole.USER);
        u.setIsActive(true);
        return u;
    }

    @Test
    @DisplayName("create: сохраняет подборку, владельца и публичный флаг по умолчанию")
    void create_happyPath_defaultsPublicTrue() {
        User author = user(1L);
        CreatePlaylistRequest req = new CreatePlaylistRequest(
                "Лучшее за 2024", "топ", null, null);

        when(userService.getById(1L)).thenReturn(author);
        when(playlistRepo.save(any(Playlist.class))).thenAnswer(inv -> {
            Playlist p = inv.getArgument(0);
            p.setId(20L);
            return p;
        });

        Playlist created = service.create(1L, req);

        assertThat(created.getId()).isEqualTo(20L);
        assertThat(created.getTitle()).isEqualTo("Лучшее за 2024");
        assertThat(created.getIsPublic()).isTrue();
        assertThat(created.getUser()).isEqualTo(author);
    }

    @Test
    @DisplayName("addItem: бросает ConflictException, если контент уже в подборке")
    void addItem_throwsConflict_whenAlreadyPresent() {
        User owner = user(1L);
        Playlist p = new Playlist();
        p.setId(20L);
        p.setUser(owner);

        AddPlaylistItemRequest req = new AddPlaylistItemRequest(99L, null);

        when(playlistRepo.findById(20L)).thenReturn(Optional.of(p));
        when(playlistRepo.containsContent(20L, 99L)).thenReturn(true);

        assertThatThrownBy(() -> service.addItem(20L, 1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже в подборке");

        verify(playlistRepo, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("getVisible: бросает ForbiddenException для приватной чужой подборки")
    void getVisible_throwsForbidden_forPrivateForeignPlaylist() {
        User owner = user(1L);
        Playlist p = new Playlist();
        p.setId(20L);
        p.setUser(owner);
        p.setIsPublic(false);

        when(playlistRepo.findById(20L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.getVisible(20L, 99L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("приватная");
    }

    @Test
    @DisplayName("getVisible: возвращает подборку владельцу, даже если приватная")
    void getVisible_returnsForOwner_evenIfPrivate() {
        User owner = user(1L);
        Playlist p = new Playlist();
        p.setId(20L);
        p.setUser(owner);
        p.setIsPublic(false);

        when(playlistRepo.findById(20L)).thenReturn(Optional.of(p));

        Playlist visible = service.getVisible(20L, 1L);

        assertThat(visible).isSameAs(p);
    }
}
