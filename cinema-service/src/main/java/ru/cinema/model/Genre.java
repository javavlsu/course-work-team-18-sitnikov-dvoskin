package ru.cinema.model;

import jakarta.persistence.*;

/**
 * Жанр контента — отдельная сущность по диаграмме классов Этапа 3.
 * (Технически в коде ниже Tag используется для всех «меток»; Genre — узкая
 * домен-специфическая категория «жанра кино».)
 */
@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 50)
    private String slug;

    @Column(name = "description", length = 300)
    private String description;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
