package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Playlist;

import java.util.List;

/**
 * Компонент доступа к данным для сущности {@link Playlist}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с пользовательскими
 * подборками контента: создание, поиск, фильтрация по публичности.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Создание подборок — создание и сохранение подборки</li>
 *     <li>Просмотр подборок — получение публичных подборок</li>
 *     <li>Добавление/удаление контента в подборку — управление содержимым</li>
 *     <li>Удаление подборки — удаление подборки и связей с контентом</li>
 *     <li>Управление профилем — просмотр своих подборок</li>
 * </ul>
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /**
     * Возвращает все подборки указанного пользователя.
     *
     * <p>Используется на странице «Мои подборки» в профиле пользователя.
     * Возвращает как публичные, так и приватные подборки.</p>
     *
     * @param userId идентификатор пользователя; должен быть положительным числом
     * @return список подборок пользователя, отсортированный по дате создания
     */
    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Возвращает публичные подборки с постраничной навигацией.
     *
     * <p>Используется на странице «Подборки» для отображения
     * доступных публичных подборок всех пользователей.</p>
     *
     * @param isPublic признак публичности ({@code true} для публичных); не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница подборок с указанным признаком публичности
     */
    Page<Playlist> findByIsPublic(Boolean isPublic, Pageable pageable);

    /**
     * Ищет подборки по подстроке в названии (без учёта регистра).
     *
     * <p>Используется для поиска подборок по ключевым словам.</p>
     *
     * @param title    подстрока для поиска в названии; не должна быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница подборок, содержащих указанную подстроку в названии
     */
    Page<Playlist> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Подсчитывает количество подборок пользователя.
     *
     * <p>Используется для отображения статистики в профиле пользователя.</p>
     *
     * @param userId идентификатор пользователя; должен быть положительным числом
     * @return количество подборок пользователя
     */
    long countByUserId(Long userId);

    /**
     * Проверяет, содержит ли подборка указанный контент.
     *
     * <p>Используется для предотвращения повторного добавления контента в подборку
     * и для отображения состояния кнопки «Добавить в подборку».</p>
     *
     * @param playlistId идентификатор подборки; должен быть положительным числом
     * @param contentId  идентификатор контента; должен быть положительным числом
     * @return {@code true}, если контент уже есть в подборке; {@code false} — иначе
     */
    @Query("SELECT COUNT(pc) > 0 FROM PlaylistContent pc " +
            "WHERE pc.playlist.id = :playlistId AND pc.content.id = :contentId")
    boolean containsContent(@Param("playlistId") Long playlistId,
                            @Param("contentId") Long contentId);
}
