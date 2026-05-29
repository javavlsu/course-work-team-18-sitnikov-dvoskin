package ru.cinema.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.auth.AuthResponse;
import ru.cinema.dto.auth.LoginRequest;
import ru.cinema.dto.auth.RefreshRequest;
import ru.cinema.dto.auth.RegisterRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.UserRepository;
import ru.cinema.security.JwtService;
import ru.cinema.service.user.UserService;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final UserService userService;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwt,
                       UserService userService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.userService = userService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new ConflictException("Username уже занят: " + req.username());
        }
        if (userRepo.existsByEmail(req.email())) {
            throw new ConflictException("Email уже занят: " + req.email());
        }
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(UserRole.USER);
        user.setIsActive(true);
        user = userRepo.save(user);
        return tokensFor(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.emailOrUsername())
                .or(() -> userRepo.findByUsername(req.emailOrUsername()))
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ForbiddenException("Аккаунт заблокирован");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }
        return tokensFor(user);
    }

    public AuthResponse refresh(RefreshRequest req) {
        try {
            Claims c = jwt.requireValid(req.refreshToken(), JwtService.TYPE_REFRESH);
            Long userId = Long.parseLong(c.getSubject());
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> NotFoundException.of("User", userId));
            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new ForbiddenException("Аккаунт заблокирован");
            }
            return tokensFor(user);
        } catch (JwtException e) {
            throw new BadCredentialsException("Невалидный refresh-токен");
        }
    }

    public void logout() {
        // Stateless JWT — реальный logout через blacklist не реализуем (выходит за рамки курсовой).
        // Клиент просто выкидывает токены.
    }

    private AuthResponse tokensFor(User user) {
        String access = jwt.generateAccessToken(user);
        String refresh = jwt.generateRefreshToken(user);
        return new AuthResponse(
                userService.toResponse(user, false),
                access,
                refresh,
                jwt.getAccessTtlSeconds()
        );
    }
}
