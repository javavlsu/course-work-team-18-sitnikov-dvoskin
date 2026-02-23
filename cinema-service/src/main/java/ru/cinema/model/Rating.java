package ru.cinema.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Сущность «Оценка».
 * <p>
 * Представляет числовую оценку контента пользователем.
 * Каждый пользователь может поставить только одну оценку конкретному контенту.
 * Значение оценки — целое число от 1 до 10.
 * </p>
 */
@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    /** Значение оценки (от 1 до 10) */
    @Column(name = "\"value\"", nullable = false)
    private Integer value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Rating() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Изменяет значение оценки.
     *
     * @param newValue новое значение оценки (от 1 до 10)
     * @throws IllegalArgumentException если значение вне диапазона 1-10
     */
    public void changeValue(Integer newValue) {
        if (newValue < 1 || newValue > 10) {
            throw new IllegalArgumentException("Оценка должна быть от 1 до 10, получено: " + newValue);
        }
        this.value = newValue;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
