package ru.cinema.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.admin.AdminUpdateUserRequest;
import ru.cinema.dto.admin.DashboardStatsResponse;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Rating;
import ru.cinema.model.Review;
import ru.cinema.model.User;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.CommentRepository;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.repository.UserRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepo;
    private final ContentRepository contentRepo;
    private final ReviewRepository reviewRepo;
    private final CommentRepository commentRepo;
    private final RatingRepository ratingRepo;

    public AdminService(UserRepository userRepo,
                        ContentRepository contentRepo,
                        ReviewRepository reviewRepo,
                        CommentRepository commentRepo,
                        RatingRepository ratingRepo) {
        this.userRepo = userRepo;
        this.contentRepo = contentRepo;
        this.reviewRepo = reviewRepo;
        this.commentRepo = commentRepo;
        this.ratingRepo = ratingRepo;
    }

    public DashboardStatsResponse stats() {
        // Users
        long usersTotal = userRepo.count();
        long usersActive = userRepo.findByIsActive(true).size();
        Map<UserRole, Long> usersByRole = new EnumMap<>(UserRole.class);
        for (UserRole r : UserRole.values()) {
            usersByRole.put(r, (long) userRepo.findByRole(r).size());
        }

        // Content
        long contentTotal = contentRepo.count();
        Map<ContentStatus, Long> contentByStatus = new EnumMap<>(ContentStatus.class);
        for (ContentStatus s : ContentStatus.values()) {
            contentByStatus.put(s, contentRepo.countByStatus(s));
        }
        Map<ContentType, Long> contentByType = new EnumMap<>(ContentType.class);
        for (ContentType t : ContentType.values()) {
            contentByType.put(t, contentRepo.countByContentType(t));
        }

        // Reviews
        long reviewsTotal = reviewRepo.count();
        Map<ReviewStatus, Long> revByStatus = new EnumMap<>(ReviewStatus.class);
        for (ReviewStatus s : ReviewStatus.values()) {
            revByStatus.put(s, reviewRepo.findByStatus(s, Pageable.unpaged()).getTotalElements());
        }
        long totalLikes = 0;
        long totalViews = 0;
        for (Review r : reviewRepo.findAll()) {
            totalLikes += r.getLikeCount() == null ? 0 : r.getLikeCount();
            totalViews += r.getViewCount() == null ? 0 : r.getViewCount();
        }

        // Comments
        long commentsTotal = commentRepo.count();

        // Ratings
        List<Rating> allRatings = ratingRepo.findAll();
        long ratingsTotal = allRatings.size();
        Double avgOverall = allRatings.isEmpty() ? null
                : allRatings.stream().mapToInt(Rating::getValue).average().orElse(0.0);

        return new DashboardStatsResponse(
                new DashboardStatsResponse.UserStats(usersTotal, usersActive, usersByRole),
                new DashboardStatsResponse.ContentStats(contentTotal, contentByStatus, contentByType),
                new DashboardStatsResponse.ReviewStats(reviewsTotal, revByStatus, totalLikes, totalViews),
                new DashboardStatsResponse.CommentStats(commentsTotal),
                new DashboardStatsResponse.RatingStats(ratingsTotal, avgOverall)
        );
    }

    public Page<User> listUsers(UserRole role, Boolean active, Pageable pageable) {
        List<User> source;
        if (role != null && active != null) {
            source = userRepo.findByRole(role).stream()
                    .filter(u -> active.equals(u.getIsActive()))
                    .toList();
        } else if (role != null) {
            source = userRepo.findByRole(role);
        } else if (active != null) {
            source = userRepo.findByIsActive(active);
        } else {
            return userRepo.findAll(pageable);
        }
        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), source.size());
        var slice = from >= source.size() ? List.<User>of() : source.subList(from, to);
        return new PageImpl<>(slice, pageable, source.size());
    }

    @Transactional
    public User updateUser(Long id, AdminUpdateUserRequest req) {
        User u = userRepo.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
        if (req.role() != null) u.setRole(req.role());
        if (req.isActive() != null) u.setIsActive(req.isActive());
        return userRepo.save(u);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) throw NotFoundException.of("User", id);
        userRepo.deleteById(id);
    }

    public Page<Content> listContent(ContentStatus status, ContentType type, Pageable pageable) {
        if (status != null && type != null) {
            return contentRepo.findByContentTypeAndStatus(type, status, pageable);
        }
        if (status != null) return contentRepo.findByStatus(status, pageable);
        return contentRepo.findAll(pageable);
    }

    public Page<Review> listReviews(ReviewStatus status, Pageable pageable) {
        if (status != null) return reviewRepo.findByStatus(status, pageable);
        return reviewRepo.findAll(pageable);
    }
}
