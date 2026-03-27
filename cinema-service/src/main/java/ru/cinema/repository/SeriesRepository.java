package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Series;
import ru.cinema.model.enums.ContentStatus;

import java.util.List;

/**
 * Компонент доступа к данным для сущности {@link Series}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с сериалами,
 * включая фильтрацию по количеству сезонов и статусу завершённости.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Просмотр контента — получение информации о сериале</li>
 *     <li>Поиск контента — фильтрация сериалов по параметрам</li>
 *     <li>Управление каталогом — CRUD-операции над сериалами</li>
 * </ul>
 */
@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

    /**
     * Возвращает опубликованные сериалы, отсортированные по дате создания (новые первые).
     *
     * <p>Используется на странице «Сериалы» для отображения каталога.</p>
     *
     * @param status   статус контента (обычно {@link ContentStatus#PUBLISHED}); не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница сериалов с указанным статусом, отсортированная по дате создания
     */
    Page<Series> findByStatusOrderByCreatedAtDesc(ContentStatus status, Pageable pageable);

    /**
     * Возвращает завершённые или продолжающиеся сериалы.
     *
     * <p>Используется для фильтрации каталога: пользователи могут
     * искать только завершённые сериалы или только продолжающиеся.</p>
     *
     * @param isFinished {@code true} — завершённые сериалы; {@code false} — продолжающиеся
     * @param status     статус контента; не должен быть {@code null}
     * @param pageable   параметры пагинации; не должен быть {@code null}
     * @return страница сериалов с указанным статусом завершённости
     */
    Page<Series> findByIsFinishedAndStatus(Boolean isFinished, ContentStatus status, Pageable pageable);

    /**
     * Возвращает сериалы с количеством сезонов не менее указанного.
     *
     * <p>Используется для фильтрации каталога по количеству сезонов.</p>
     *
     * @param minSeasons минимальное количество сезонов (включительно); должно быть >= 1
     * @param pageable   параметры пагинации; не должен быть {@code null}
     * @return страница сериалов с количеством сезонов >= {@code minSeasons}
     */
    Page<Series> findByTotalSeasonsGreaterThanEqual(Integer minSeasons, Pageable pageable);

    /**
     * Возвращает сериалы указанного года выпуска.
     *
     * <p>Используется для фильтрации каталога сериалов по году.</p>
     *
     * @param releaseYear год выпуска; должен быть положительным числом
     * @param status      статус контента; не должен быть {@code null}
     * @return список сериалов указанного года выпуска
     */
    List<Series> findByReleaseYearAndStatus(Integer releaseYear, ContentStatus status);
}
