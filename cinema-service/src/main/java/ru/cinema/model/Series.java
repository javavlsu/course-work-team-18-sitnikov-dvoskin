package ru.cinema.model;

import jakarta.persistence.*;

/**
 * Сущность «Сериал».
 * <p>
 * Наследует {@link Content} и дополняет его атрибутами,
 * специфичными для сериалов: количество сезонов, эпизодов, статус завершённости.
 * </p>
 */
@Entity
@Table(name = "series")
public class Series extends Content {

    /** Общее количество сезонов */
    @Column(name = "total_seasons")
    private Integer totalSeasons;

    /** Общее количество эпизодов */
    @Column(name = "total_episodes")
    private Integer totalEpisodes;

    /** Признак завершённости сериала */
    @Column(name = "is_finished")
    private Boolean isFinished;

    public Series() {
    }

    public Integer getTotalSeasons() { return totalSeasons; }
    public void setTotalSeasons(Integer totalSeasons) { this.totalSeasons = totalSeasons; }

    public Integer getTotalEpisodes() { return totalEpisodes; }
    public void setTotalEpisodes(Integer totalEpisodes) { this.totalEpisodes = totalEpisodes; }

    public Boolean getIsFinished() { return isFinished; }
    public void setIsFinished(Boolean isFinished) { this.isFinished = isFinished; }
}
