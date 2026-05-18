package ru.cinema.service.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.review.CreateReviewRequest;
import ru.cinema.dto.review.UpdateReviewRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Review;
import ru.cinema.model.User;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.model.enums.UserRole;
import ru.cinema.dto.review.LikeResponse;
import ru.cinema.model.ReviewLike;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.ReviewLikeRepository;
import ru.cinema.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ContentRepository contentRepo;
    private final ReviewLikeRepository likeRepo;
    private final ru.cinema.service.user.UserService userService;

    public ReviewService(ReviewRepository reviewRepo,
                         ContentRepository contentRepo,
                         ReviewLikeRepository likeRepo,
                         ru.cinema.service.user.UserService userService) {
        this.reviewRepo = reviewRepo;
        this.contentRepo = contentRepo;
        this.likeRepo = likeRepo;
        this.userService = userService;
    }

    public Review getById(Long id) {
        return reviewRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("Review", id));
    }

    public Page<Review> list(Long contentId, Long userId, ReviewStatus status, Pageable pageable) {
        if (contentId != null) {
            return reviewRepo.findByContentIdAndStatus(contentId,
                    status == null ? ReviewStatus.PUBLISHED : status, pageable);
        }
        if (userId != null) {
            return status == null
                    ? reviewRepo.findByUserId(userId, pageable)
                    : reviewRepo.findByUserIdAndStatus(userId, status, pageable);
        }
        return reviewRepo.findByStatus(status == null ? ReviewStatus.PUBLISHED : status, pageable);
    }

    @Transactional
    public Review create(Long currentUserId, CreateReviewRequest req) {
        Content content = contentRepo.findById(req.contentId())
                .orElseThrow(() -> NotFoundException.of("Content", req.contentId()));
        if (reviewRepo.existsByUserIdAndContentId(currentUserId, req.contentId())) {
            throw new ConflictException("Вы уже написали рецензию на этот контент");
        }
        Review r = new Review();
        r.setUser(userService.getById(currentUserId));
        r.setContent(content);
        r.setTitle(req.title());
        r.setText(req.text());
        r.setRatingValue(req.ratingValue());
        r.setStatus(ReviewStatus.DRAFT);
        return reviewRepo.save(r);
    }

    @Transactional
    public Review update(Long id, Long currentUserId, UpdateReviewRequest req) {
        Review r = getById(id);
        ensureAuthor(r, currentUserId);
        if (req.title() != null) r.setTitle(req.title());
        if (req.text() != null) r.setText(req.text());
        if (req.ratingValue() != null) r.setRatingValue(req.ratingValue());
        return reviewRepo.save(r);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        Review r = getById(id);
        User u = userService.getById(currentUserId);
        if (u.getRole() != UserRole.ADMIN && !r.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Удалить может только автор или ADMIN");
        }
        reviewRepo.delete(r);
    }

    @Transactional
    public Review publish(Long id, Long currentUserId) {
        Review r = getById(id);
        ensureAuthor(r, currentUserId);
        User u = userService.getById(currentUserId);
        // ADMIN-автор сразу публикует, USER — отправляет на модерацию
        r.setStatus(u.getRole() == UserRole.ADMIN ? ReviewStatus.PUBLISHED : ReviewStatus.MODERATION);
        return reviewRepo.save(r);
    }

    /**
     * Toggle лайка. Один пользователь — один лайк на рецензию (гарант. uq_review_likes).
     * Пересчитывает denormalized like_count из COUNT(*) review_likes для рецензии.
     */
    @Transactional
    public LikeResponse toggleLike(Long reviewId, Long userId) {
        Review r = getById(reviewId);
        User u = userService.getById(userId);
        boolean liked;
        if (likeRepo.existsByReview_IdAndUser_Id(reviewId, userId)) {
            likeRepo.deleteByReviewIdAndUserId(reviewId, userId);
            liked = false;
        } else {
            ReviewLike rl = new ReviewLike();
            rl.setReview(r);
            rl.setUser(u);
            likeRepo.save(rl);
            liked = true;
        }
        long count = likeRepo.countByReview_Id(reviewId);
        r.setLikeCount((int) count);
        reviewRepo.save(r);
        return new LikeResponse(liked, count);
    }

    public boolean hasLiked(Long reviewId, Long userId) {
        if (userId == null) return false;
        return likeRepo.existsByReview_IdAndUser_Id(reviewId, userId);
    }

    @Transactional
    public Review incrementView(Long id) {
        Review r = getById(id);
        r.setViewCount((r.getViewCount() == null ? 0 : r.getViewCount()) + 1);
        return reviewRepo.save(r);
    }

    @Transactional
    public Review updateStatus(Long id, ReviewStatus status) {
        return updateStatus(id, status, null);
    }

    @Transactional
    public Review updateStatus(Long id, ReviewStatus status, String reason) {
        Review r = getById(id);
        r.setStatus(status);
        // Причина сохраняется только при REJECTED/HIDDEN — для других статусов сбрасываем.
        if (status == ReviewStatus.REJECTED || status == ReviewStatus.HIDDEN) {
            r.setModerationReason(reason);
        } else {
            r.setModerationReason(null);
        }
        return reviewRepo.save(r);
    }

    private void ensureAuthor(Review r, Long userId) {
        if (!r.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Действие доступно только автору рецензии");
        }
    }
}
