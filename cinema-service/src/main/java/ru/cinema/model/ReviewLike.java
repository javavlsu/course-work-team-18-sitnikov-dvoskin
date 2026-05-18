package ru.cinema.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Лайк к рецензии. Уникальная связка (review_id, user_id) — БД-констрейнт
 * uq_review_likes гарантирует, что один пользователь не может «спамить» лайки.
 */
@Entity
@Table(name = "review_likes",
        uniqueConstraints = @UniqueConstraint(name = "uq_review_likes", columnNames = {"review_id", "user_id"}))
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
