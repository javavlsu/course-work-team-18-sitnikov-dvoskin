package ru.cinema.service.rating;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.rating.RatingResponse;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Rating;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepo;
    private final ContentRepository contentRepo;
    private final ru.cinema.service.user.UserService userService;

    public RatingService(RatingRepository ratingRepo,
                         ContentRepository contentRepo,
                         ru.cinema.service.user.UserService userService) {
        this.ratingRepo = ratingRepo;
        this.contentRepo = contentRepo;
        this.userService = userService;
    }

    public Optional<RatingResponse> myRating(Long userId, Long contentId) {
        return ratingRepo.findByUserIdAndContentId(userId, contentId).map(RatingResponse::of);
    }

    /** Все оценки пользователя — для /api/v1/users/me/ratings (API map Этап 8). */
    public java.util.List<RatingResponse> listByUser(Long userId) {
        return ratingRepo.findByUserId(userId).stream().map(RatingResponse::of).toList();
    }

    @Transactional
    public RatingResponse rate(Long userId, Long contentId, Integer value) {
        Content content = contentRepo.findById(contentId)
                .orElseThrow(() -> NotFoundException.of("Content", contentId));
        Rating rating = ratingRepo.findByUserIdAndContentId(userId, contentId)
                .orElseGet(() -> {
                    Rating r = new Rating();
                    r.setUser(userService.getById(userId));
                    r.setContent(content);
                    return r;
                });
        rating.setValue(value);
        rating = ratingRepo.save(rating);
        recalcAverage(content);
        return RatingResponse.of(rating);
    }

    @Transactional
    public void delete(Long userId, Long contentId) {
        Rating r = ratingRepo.findByUserIdAndContentId(userId, contentId)
                .orElseThrow(() -> new NotFoundException("Оценка не найдена"));
        ratingRepo.delete(r);
        recalcAverage(r.getContent());
    }

    private void recalcAverage(Content c) {
        Double avg = ratingRepo.calculateAverageRating(c.getId());
        if (avg == null) {
            c.setAverageRating(BigDecimal.ZERO);
        } else {
            c.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }
        contentRepo.save(c);
    }
}
