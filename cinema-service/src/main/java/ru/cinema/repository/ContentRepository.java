package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Content;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;

import java.util.List;
import java.util.Optional;

/**
 * Компонент доступа к данным для сущности {@link Content}.
 *
 * <p><b>Назначение:</b> предоставляет методы для выполнения CRUD-операций
 * и специализированных запросов к каталогу контента (фильмы и сериалы).</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Просмотр контента — получение информации о фильме/сериале</li>
 *     <li>Поиск контента — поиск по названию, жанру, году выпуска</li>
 *     <li>Управление каталогом (администратор) — добавление, редактирование, удаление контента</li>
 *     <li>Модерация контента — фильтрация по статусу</li>
 *     <li>Просмотр результатов — постраничный вывод контента</li>
 * </ul>
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Long>, JpaSpecificationExecutor<Content> {

    /** Подсчёт по типу контента. */
    long countByContentType(ContentType contentType);

    /**
     * Возвращает постраничный список контента с указанным статусом.
     *
     * <p>Используется для отображения каталога (статус PUBLISHED)
     * и для модерации (статус MODERATION).</p>
     *
     * @param status статус контента ({@link ContentStatus}); не должен быть {@code null}
     * @param pageable параметры пагинации и сортировки; не должен быть {@code null}
     * @return страница контента с указанным статусом
     */
    Page<Content> findByStatus(ContentStatus status, Pageable pageable);

    /**
     * Возвращает постраничный список контента указанного типа и статуса.
     *
     * <p>Используется для раздельного отображения фильмов и сериалов в каталоге.</p>
     *
     * @param contentType тип контента ({@link ContentType}); не должен быть {@code null}
     * @param status      статус контента ({@link ContentStatus}); не должен быть {@code null}
     * @param pageable    параметры пагинации и сортировки; не должен быть {@code null}
     * @return страница контента указанного типа и статуса
     */
    Page<Content> findByContentTypeAndStatus(ContentType contentType, ContentStatus status, Pageable pageable);

    /**
     * Ищет контент по подстроке в названии (без учёта регистра).
     *
     * <p>Используется в функции поиска контента. Поиск выполняется
     * и по русскому названию ({@code title}), и по оригинальному ({@code originalTitle}).</p>
     *
     * @param title    подстрока для поиска в названии; не должна быть {@code null}
     * @param status   статус контента (обычно {@link ContentStatus#PUBLISHED}); не должен быть {@code null}
     * @param pageable параметры пагинации и сортировки; не должен быть {@code null}
     * @return страница контента, в названии которого содержится указанная подстрока
     */
    @Query("SELECT c FROM Content c WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "OR LOWER(c.originalTitle) LIKE LOWER(CONCAT('%', :title, '%'))) AND c.status = :status")
    Page<Content> searchByTitle(@Param("title") String title,
                                @Param("status") ContentStatus status,
                                Pageable pageable);

    /**
     * Возвращает контент определённого года выпуска с указанным статусом.
     *
     * <p>Используется для фильтрации каталога по году выпуска.</p>
     *
     * @param releaseYear год выпуска; должен быть положительным числом
     * @param status      статус контента; не должен быть {@code null}
     * @param pageable    параметры пагинации и сортировки; не должен быть {@code null}
     * @return страница контента указанного года выпуска
     */
    Page<Content> findByReleaseYearAndStatus(Integer releaseYear, ContentStatus status, Pageable pageable);

    /**
     * Возвращает контент определённой страны производства.
     *
     * <p>Используется для фильтрации каталога по стране.</p>
     *
     * @param country  название страны; не должно быть {@code null}
     * @param status   статус контента; не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница контента из указанной страны
     */
    Page<Content> findByCountryAndStatus(String country, ContentStatus status, Pageable pageable);

    /**
     * Возвращает топ контента по среднему рейтингу.
     *
     * <p>Используется для раздела «Лучшие фильмы/сериалы» на главной странице.</p>
     *
     * @param status   статус контента (обычно {@link ContentStatus#PUBLISHED}); не должен быть {@code null}
     * @param pageable параметры пагинации (для ограничения количества); не должен быть {@code null}
     * @return страница контента, отсортированная по убыванию среднего рейтинга
     */
    Page<Content> findByStatusOrderByAverageRatingDesc(ContentStatus status, Pageable pageable);

    /**
     * Находит контент по идентификатору IMDb.
     *
     * <p>Используется для интеграции с внешними источниками данных
     * и предотвращения дублирования контента.</p>
     *
     * @param imdbId идентификатор IMDb (например, "tt1234567"); может быть {@code null}
     * @return {@link Optional} с найденным контентом, или пустой {@link Optional}
     */
    Optional<Content> findByImdbId(String imdbId);

    /**
     * Находит контент по идентификатору Кинопоиска.
     *
     * <p>Используется для интеграции с Кинопоиском
     * и предотвращения дублирования контента.</p>
     *
     * @param kinopoiskId идентификатор Кинопоиска; может быть {@code null}
     * @return {@link Optional} с найденным контентом, или пустой {@link Optional}
     */
    Optional<Content> findByKinopoiskId(String kinopoiskId);

    /**
     * Находит контент, помеченный указанным тегом.
     *
     * <p>Используется для навигации по каталогу через теги (жанры).</p>
     *
     * @param tagId    идентификатор тега; должен быть положительным числом
     * @param status   статус контента; не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница контента с указанным тегом
     */
    @Query("SELECT c FROM Content c JOIN c.tags t WHERE t.id = :tagId AND c.status = :status " +
           "AND c.posterBroken = false AND c.posterUrl IS NOT NULL AND length(c.posterUrl) > 0")
    Page<Content> findByTagId(@Param("tagId") Long tagId,
                              @Param("status") ContentStatus status,
                              Pageable pageable);

    /**
     * Подсчитывает общее количество контента с указанным статусом.
     *
     * <p>Используется в аналитике системы для статистики каталога.</p>
     *
     * @param status статус контента; не должен быть {@code null}
     * @return количество единиц контента с указанным статусом
     */
    long countByStatus(ContentStatus status);

    // =================================================================
    // Расширения be-fts / be-recs
    // =================================================================

    /**
     * PostgreSQL full-text search — шаг 1: нативный запрос вернёт только id, отсортированные по
     * релевантности (ts_rank). Шаг 2 ({@link #findAllById(Iterable)}) поднимает полноценные
     * JOINED-сущности (Movie / Series). Нативный запрос на JOINED-иерархии без явного
     * SqlResultSetMapping ломается на discriminator-колонке clazz_, поэтому работаем через id.
     *
     * <p>Использует словарь 'russian' и GIN-индекс idx_content_fts. Только PUBLISHED-контент.</p>
     */
    @Query(
            value = """
                    SELECT c.id FROM content c
                    WHERE c.status = 'PUBLISHED'
                      AND c.poster_broken = FALSE
                      AND c.poster_url IS NOT NULL
                      AND c.poster_url <> ''
                      AND to_tsvector('russian',
                              coalesce(c.title,'') || ' ' ||
                              coalesce(c.original_title,'') || ' ' ||
                              coalesce(c.description,'')
                          ) @@ plainto_tsquery('russian', :q)
                    ORDER BY ts_rank(
                              to_tsvector('russian',
                                  coalesce(c.title,'') || ' ' ||
                                  coalesce(c.original_title,'') || ' ' ||
                                  coalesce(c.description,'')),
                              plainto_tsquery('russian', :q)
                          ) DESC
                    """,
            countQuery = """
                    SELECT count(*) FROM content c
                    WHERE c.status = 'PUBLISHED'
                      AND c.poster_broken = FALSE
                      AND c.poster_url IS NOT NULL
                      AND c.poster_url <> ''
                      AND to_tsvector('russian',
                              coalesce(c.title,'') || ' ' ||
                              coalesce(c.original_title,'') || ' ' ||
                              coalesce(c.description,'')
                          ) @@ plainto_tsquery('russian', :q)
                    """,
            nativeQuery = true
    )
    Page<Long> fullTextSearchIds(@Param("q") String q, Pageable pageable);

    /**
     * Загружает все PUBLISHED-объекты вместе с тегами одной выборкой.
     *
     * <p>Используется алгоритмом рекомендаций (content-based) для построения
     * векторов «контент → теги» без N+1 запросов.</p>
     */
    @Query("SELECT DISTINCT c FROM Content c LEFT JOIN FETCH c.tags LEFT JOIN FETCH c.genres WHERE c.status = :status")
    List<Content> findAllPublishedWithTags(@Param("status") ContentStatus status);

    /**
     * Возвращает PUBLISHED-контент, исключая указанные id.
     *
     * <p>Используется CF-алгоритмом рекомендаций для исключения уже оценённых юзером.</p>
     */
    List<Content> findAllByStatusAndIdNotIn(ContentStatus status, java.util.Collection<Long> excludeIds);
}
