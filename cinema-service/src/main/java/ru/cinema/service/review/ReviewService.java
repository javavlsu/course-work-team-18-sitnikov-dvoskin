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
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ContentRepository contentRepo;
    private final ru.cinema.service.user.UserService userService;

    public ReviewService(ReviewRepository reviewRepo,
                         ContentRepository contentRepo,
                         ru.cinema.service.user.UserService userService) {
        this.reviewRepo = reviewRepo;
        this.contentRepo = contentRepo;
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
            return reviewRepo.findByUserId(userId, pageable);
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

    @Transactional
    public Review like(Long id) {
        Review r = getById(id);
        r.setLikeCount((r.getLikeCount() == null ? 0 : r.getLikeCount()) + 1);
        return reviewRepo.save(r);
    }

    @Transactional
    public Review incrementView(Long id) {
        Review r = getById(id);
        r.setViewCount((r.getViewCount() == null ? 0 : r.getViewCount()) + 1);
        return reviewRepo.save(r);
    }

    @Transactional
    public Review updateStatus(Long id, ReviewStatus status) {
        Review r = getById(id);
        r.setStatus(status);
        return reviewRepo.save(r);
    }

    private void ensureAuthor(Review r, Long userId) {
        if (!r.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Действие доступно только автору рецензии");
        }
    }
}
