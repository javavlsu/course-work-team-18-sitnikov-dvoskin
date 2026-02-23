package ru.cinema.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Сущность связи «Подборка — Контент».
 * <p>
 * Промежуточная таблица для связи many-to-many между подборками и контентом.
 * Хранит дополнительные атрибуты: дату добавления и порядок сортировки.
 * </p>
 */
@Entity
@Table(name = "playlist_content")
@IdClass(PlaylistContentId.class)
public class PlaylistContent {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public PlaylistContent() {
    }

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) addedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
