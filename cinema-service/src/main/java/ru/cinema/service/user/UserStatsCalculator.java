package ru.cinema.service.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.user.UserStats;
import ru.cinema.model.Rating;
import ru.cinema.model.User;
import ru.cinema.repository.PlaylistRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.repository.ReviewRepository;

import java.util.List;

/**
 * Считает агрегированную статистику профиля пользователя.
 * Вынесен в отдельный компонент, чтобы UserService и PublicUserService использовали одно и то же.
 */
@Component
@Transactional(readOnly = true)
public class UserStatsCalculator {

    private final ReviewRepository reviewRepo;
    private final PlaylistRepository playlistRepo;
    private final RatingRepository ratingRepo;

    public UserStatsCalculator(ReviewRepository reviewRepo,
                               PlaylistRepository playlistRepo,
                               RatingRepository ratingRepo) {
        this.reviewRepo = reviewRepo;
        this.playlistRepo = playlistRepo;
        this.ratingRepo = ratingRepo;
    }

    public UserStats statsFor(User user) {
        long reviewsCount = reviewRepo.findByUserId(user.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long playlistsCount = playlistRepo.countByUserId(user.getId());
        List<Rating> ratings = ratingRepo.findByUserId(user.getId());
        long ratingsCount = ratings.size();
        Double avg = ratings.isEmpty() ? null
                : ratings.stream().mapToInt(Rating::getValue).average().orElse(0.0);
        return new UserStats(reviewsCount, playlistsCount, ratingsCount, avg);
    }
}
