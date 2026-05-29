package ru.cinema.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.user.PublicUserResponse;
import ru.cinema.dto.user.UpdateUserRequest;
import ru.cinema.dto.user.UserResponse;
import ru.cinema.dto.user.UserStats;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Playlist;
import ru.cinema.model.Review;
import ru.cinema.model.User;
import ru.cinema.repository.PlaylistRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.repository.UserRepository;

import java.util.List;

/**
 * Сервис управления пользователями: профиль, обновления, публичный доступ.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;
    private final PlaylistRepository playlistRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserStatsCalculator statsCalc;

    public UserService(UserRepository userRepo,
                       ReviewRepository reviewRepo,
                       PlaylistRepository playlistRepo,
                       PasswordEncoder passwordEncoder,
                       UserStatsCalculator statsCalc) {
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
        this.playlistRepo = playlistRepo;
        this.passwordEncoder = passwordEncoder;
        this.statsCalc = statsCalc;
    }

    public UserResponse toResponse(User user, boolean withStats) {
        UserStats stats = withStats ? statsCalc.statsFor(user) : null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                stats
        );
    }

    public PublicUserResponse toPublicResponse(User user) {
        return new PublicUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt(),
                statsCalc.statsFor(user)
        );
    }

    public User getById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
    }

    public User getByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + username));
    }

    @Transactional
    public UserResponse updateProfile(Long currentUserId, UpdateUserRequest req) {
        User user = getById(currentUserId);

        // username
        if (req.username() != null && !req.username().equals(user.getUsername())) {
            if (userRepo.existsByUsername(req.username())) {
                throw new ConflictException("Username уже занят: " + req.username());
            }
            user.setUsername(req.username());
        }

        // email
        if (req.email() != null && !req.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepo.existsByEmail(req.email())) {
                throw new ConflictException("Email уже занят: " + req.email());
            }
            user.setEmail(req.email());
        }

        // password
        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            if (req.currentPassword() == null
                    || !passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
                throw new ForbiddenException("Текущий пароль неверен");
            }
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        }

        userRepo.save(user);
        return toResponse(user, true);
    }

    public Page<Review> getUserReviews(String username, Pageable pageable) {
        User u = getByUsername(username);
        return reviewRepo.findByUserId(u.getId(), pageable);
    }

    public Page<Review> getUserReviewsById(Long userId, Pageable pageable) {
        return reviewRepo.findByUserId(userId, pageable);
    }

    public List<Playlist> getUserPlaylists(String username) {
        User u = getByUsername(username);
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(u.getId());
    }

    public List<Playlist> getUserPlaylistsById(Long userId) {
        return playlistRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
