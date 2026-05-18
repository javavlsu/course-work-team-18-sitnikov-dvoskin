package ru.cinema.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Оценка подборки — сущность CollectionRating из Этапа 3.
 * Один пользователь — одна оценка на подборку (uq_pr_user_playlist).
 */
@Entity
@Table(name = "playlist_ratings",
        uniqueConstraints = @UniqueConstraint(name = "uq_pr_user_playlist",
                                              columnNames = {"user_id", "playlist_id"}))
public class PlaylistRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist p) { this.playlist = p; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
