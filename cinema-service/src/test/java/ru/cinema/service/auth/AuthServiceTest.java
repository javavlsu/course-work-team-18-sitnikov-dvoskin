package ru.cinema.service.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.cinema.dto.auth.AuthResponse;
import ru.cinema.dto.auth.LoginRequest;
import ru.cinema.dto.auth.RegisterRequest;
import ru.cinema.dto.user.UserResponse;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.UserRepository;
import ru.cinema.security.JwtService;
import ru.cinema.service.user.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link AuthService}.
 *
 * <p>Покрытие: register (happy + дубликат), login (happy + неверный пароль + блокированный аккаунт).</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwt;
    @Mock UserService userService;
    @Mock Claims claims;

    @InjectMocks AuthService service;

    private User sampleUser() {
        User u = new User();
        u.setId(1L);
        u.setUsername("byte");
        u.setEmail("byte@example.com");
        u.setPasswordHash("$2a$hash");
        u.setRole(UserRole.USER);
        u.setIsActive(true);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    @Test
    @DisplayName("register: создаёт пользователя и возвращает токены")
    void register_happyPath() {
        RegisterRequest req = new RegisterRequest("byte", "byte@example.com", "password123");

        when(userRepo.existsByUsername("byte")).thenReturn(false);
        when(userRepo.existsByEmail("byte@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hash");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwt.generateAccessToken(any(User.class))).thenReturn("ACCESS");
        when(jwt.generateRefreshToken(any(User.class))).thenReturn("REFRESH");
        when(jwt.getAccessTtlSeconds()).thenReturn(3600L);
        when(userService.toResponse(any(User.class), anyBoolean()))
                .thenReturn(new UserResponse(1L, "byte", "byte@example.com",
                        UserRole.USER, true, LocalDateTime.now(), null));

        AuthResponse resp = service.register(req);

        assertThat(resp.accessToken()).isEqualTo("ACCESS");
        assertThat(resp.refreshToken()).isEqualTo("REFRESH");
        assertThat(resp.expiresIn()).isEqualTo(3600L);
        assertThat(resp.user().username()).isEqualTo("byte");
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("register: бросает ConflictException, если username занят")
    void register_throwsConflict_whenUsernameTaken() {
        RegisterRequest req = new RegisterRequest("byte", "byte@example.com", "password123");
        when(userRepo.existsByUsername("byte")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username уже занят");

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login: возвращает токены при корректных credentials")
    void login_happyPath() {
        LoginRequest req = new LoginRequest("byte@example.com", "password123");
        User user = sampleUser();

        when(userRepo.findByEmail("byte@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$hash")).thenReturn(true);
        when(jwt.generateAccessToken(user)).thenReturn("ACCESS");
        when(jwt.generateRefreshToken(user)).thenReturn("REFRESH");
        when(jwt.getAccessTtlSeconds()).thenReturn(3600L);
        when(userService.toResponse(user, false))
                .thenReturn(new UserResponse(1L, "byte", "byte@example.com",
                        UserRole.USER, true, user.getCreatedAt(), null));

        AuthResponse resp = service.login(req);

        assertThat(resp.accessToken()).isEqualTo("ACCESS");
        assertThat(resp.user().id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("login: бросает BadCredentialsException при неверном пароле")
    void login_throwsBadCredentials_whenWrongPassword() {
        LoginRequest req = new LoginRequest("byte@example.com", "wrong");
        User user = sampleUser();

        when(userRepo.findByEmail("byte@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Неверный логин или пароль");
    }

    @Test
    @DisplayName("login: бросает ForbiddenException, если аккаунт заблокирован")
    void login_throwsForbidden_whenAccountBlocked() {
        LoginRequest req = new LoginRequest("byte@example.com", "password123");
        User user = sampleUser();
        user.setIsActive(false);

        when(userRepo.findByEmail("byte@example.com")).thenReturn(Optional.of(user));
        // passwordEncoder.matches не вызывается до проверки isActive — используем lenient на всякий
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("заблокирован");
    }
}
