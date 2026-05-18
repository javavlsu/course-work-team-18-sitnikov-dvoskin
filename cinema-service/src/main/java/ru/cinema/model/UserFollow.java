package ru.cinema.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Подписка пользователя на другого пользователя — сущность из диаграммы Этапа 3.
 * Composite PK (follower, following). Один юзер не может подписаться сам на себя
 * (CHECK constraint на уровне БД).
 */
@Entity
@Table(name = "user_follows")
@IdClass(UserFollow.PK.class)
public class UserFollow {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public User getFollower() { return follower; }
    public void setFollower(User follower) { this.follower = follower; }
    public User getFollowing() { return following; }
    public void setFollowing(User following) { this.following = following; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class PK implements Serializable {
        private Long follower;
        private Long following;

        public PK() {}
        public PK(Long follower, Long following) {
            this.follower = follower; this.following = following;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(follower, pk.follower) && Objects.equals(following, pk.following);
        }
        @Override public int hashCode() { return Objects.hash(follower, following); }
    }
}
