package ru.cinema.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность «Подборка».
 * <p>
 * Представляет пользовательскую подборку контента (фильмов и сериалов).
 * Подборка может быть публичной или приватной.
 * Связь с контентом реализована через промежуточную сущность {@link PlaylistContent}.
 * </p>
 */
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistContent> playlistContents = new ArrayList<>();

    public Playlist() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isPublic == null) isPublic = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Добавляет контент в подборку с указанием порядка сортировки.
     *
     * @param content контент для добавления
     */
    public void addContent(Content content) {
        int nextOrder = playlistContents.size() + 1;
        PlaylistContent pc = new PlaylistContent();
        pc.setPlaylist(this);
        pc.setContent(content);
        pc.setSortOrder(nextOrder);
        pc.setAddedAt(LocalDateTime.now());
        playlistContents.add(pc);
    }

    /**
     * Удаляет контент из подборки.
     *
     * @param content контент для удаления
     */
    public void removeContent(Content content) {
        playlistContents.removeIf(pc -> pc.getContent().getId().equals(content.getId()));
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PlaylistContent> getPlaylistContents() { return playlistContents; }
    public void setPlaylistContents(List<PlaylistContent> playlistContents) { this.playlistContents = playlistContents; }
}
