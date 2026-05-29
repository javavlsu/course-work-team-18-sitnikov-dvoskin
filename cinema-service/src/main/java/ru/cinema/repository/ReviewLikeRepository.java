package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cinema.model.ReviewLike;

import java.util.Set;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReview_IdAndUser_Id(Long reviewId, Long userId);

    @Modifying
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.id = :reviewId AND rl.user.id = :userId")
    int deleteByReviewIdAndUserId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);

    long countByReview_Id(Long reviewId);

    @Query("SELECT rl.review.id FROM ReviewLike rl WHERE rl.user.id = :userId AND rl.review.id IN :reviewIds")
    Set<Long> findLikedReviewIds(@Param("userId") Long userId, @Param("reviewIds") Iterable<Long> reviewIds);
}
