package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Movie;
import ru.cinema.model.enums.ContentStatus;

import java.util.List;

/**
 * Компонент доступа к данным для сущности {@link Movie}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с фильмами,
 * включая фильтрацию по продолжительности и финансовым показателям.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Просмотр контента — получение информации о фильме</li>
 *     <li>Поиск контента — фильтрация фильмов по длительности</li>
 *     <li>Управление каталогом — CRUD-операции над фильмами</li>
 * </ul>
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Возвращает фильмы с продолжительностью в заданном диапазоне.
     *
     * <p>Используется при фильтрации каталога по длительности фильма.</p>
     *
     * @param minDuration минимальная продолжительность в минутах (включительно);
     *                    должна быть >= 0
     * @param maxDuration максимальная продолжительность в минутах (включительно);
     *                    должна быть >= {@code minDuration}
     * @param pageable    параметры пагинации; не должен быть {@code null}
     * @return страница фильмов с продолжительностью в указанном диапазоне
     */
    Page<Movie> findByDurationBetween(Integer minDuration, Integer maxDuration, Pageable pageable);

    /**
     * Возвращает опубликованные фильмы, отсортированные по дате создания (новые первые).
     *
     * <p>Используется на странице «Фильмы» для отображения каталога.</p>
     *
     * @param status   статус контента (обычно {@link ContentStatus#PUBLISHED}); не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница фильмов с указанным статусом, отсортированная по дате создания
     */
    Page<Movie> findByStatusOrderByCreatedAtDesc(ContentStatus status, Pageable pageable);

    /**
     * Возвращает фильмы указанного года выпуска.
     *
     * <p>Используется для фильтрации каталога фильмов по году.</p>
     *
     * @param releaseYear год выпуска; должен быть положительным числом
     * @param status      статус контента; не должен быть {@code null}
     * @return список фильмов указанного года выпуска с заданным статусом
     */
    List<Movie> findByReleaseYearAndStatus(Integer releaseYear, ContentStatus status);
}
