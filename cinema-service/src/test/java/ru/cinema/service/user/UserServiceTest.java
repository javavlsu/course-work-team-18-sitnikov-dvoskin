package ru.cinema.service.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.cinema.dto.user.UpdateUserRequest;
import ru.cinema.dto.user.UserResponse;
import ru.cinema.dto.user.UserStats;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.PlaylistRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link UserService}: профиль, обновление, конфликт по username.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepo;
    @Mock ReviewRepository reviewRepo;
    @Mock PlaylistRepository playlistRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserStatsCalculator statsCalc;

    @InjectMocks UserService service;

    private User sampleUser() {
        User u = new User();
        u.setId(1L);
        u.setUsername("byte");
        u.setEmail("byte@example.com");
        u.setPasswordHash("$2a$old");
        u.setRole(UserRole.USER);
        u.setIsActive(true);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    @Test
    @DisplayName("getById: возвращает пользователя, если найден")
    void getById_returnsUser_whenExists() {
        User user = sampleUser();
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User result = service.getById(1L);

        assertThat(result.getUsername()).isEqualTo("byte");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById: бросает NotFoundException для несуществующего id")
    void getById_throwsNotFound_whenMissing() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("toResponse: с withStats=true вычисляет статистику через калькулятор")
    void toResponse_includesStats_whenRequested() {
        User user = sampleUser();
        UserStats stats = new UserStats(5L, 2L, 10L, 7.5);
        when(statsCalc.statsFor(user)).thenReturn(stats);

        UserResponse resp = service.toResponse(user, true);

        assertThat(resp.username()).isEqualTo("byte");
        assertThat(resp.stats()).isEqualTo(stats);
    }

    @Test
    @DisplayName("updateProfile: обновляет username, если он не занят")
    void updateProfile_updatesUsername_whenAvailable() {
        User user = sampleUser();
        UpdateUserRequest req = new UpdateUserRequest("newbyte", null, null, null);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsername("newbyte")).thenReturn(false);
        when(statsCalc.statsFor(user)).thenReturn(new UserStats(0L, 0L, 0L, null));

        UserResponse resp = service.updateProfile(1L, req);

        assertThat(resp.username()).isEqualTo("newbyte");
        verify(userRepo).save(user);
    }

    @Test
    @DisplayName("updateProfile: бросает ConflictException, если username занят")
    void updateProfile_throwsConflict_whenUsernameTaken() {
        User user = sampleUser();
        UpdateUserRequest req = new UpdateUserRequest("taken", null, null, null);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfile(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username уже занят");

        verify(userRepo, never()).save(any(User.class));
    }
}
