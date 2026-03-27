package ru.cinema.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Сущность «Фильм».
 * <p>
 * Наследует {@link Content} и дополняет его атрибутами,
 * специфичными для фильмов: продолжительность, бюджет, кассовые сборы.
 * </p>
 */
@Entity
@Table(name = "movies")
public class Movie extends Content {

    /** Продолжительность фильма в минутах */
    @Column(name = "duration")
    private Integer duration;

    /** Бюджет фильма (в долларах) */
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    /** Кассовые сборы фильма (в долларах) */
    @Column(name = "box_office", precision = 15, scale = 2)
    private BigDecimal boxOffice;

    public Movie() {
    }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public BigDecimal getBoxOffice() { return boxOffice; }
    public void setBoxOffice(BigDecimal boxOffice) { this.boxOffice = boxOffice; }
}
