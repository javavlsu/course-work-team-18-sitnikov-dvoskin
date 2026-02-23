package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Tag;

import java.util.List;
import java.util.Optional;

/**
 * Компонент доступа к данным для сущности {@link Tag}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с тегами (жанрами),
 * используемыми для классификации контента.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Поиск контента — фильтрация контента по тегам/жанрам</li>
 *     <li>Управление каталогом — назначение тегов контенту</li>
 *     <li>Просмотр контента — отображение тегов на карточке контента</li>
 * </ul>
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Находит тег по имени.
     *
     * <p>Используется при назначении тега контенту для проверки существования.</p>
     *
     * @param name имя тега; не должно быть {@code null} или пустым
     * @return {@link Optional} с найденным тегом, или пустой {@link Optional}
     */
    Optional<Tag> findByName(String name);

    /**
     * Находит тег по слагу (URL-friendly имени).
     *
     * <p>Используется для формирования ЧПУ-ссылок на страницы тегов.</p>
     *
     * @param slug слаг тега (например, "sci-fi"); не должен быть {@code null}
     * @return {@link Optional} с найденным тегом, или пустой {@link Optional}
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * Проверяет, существует ли тег с указанным именем.
     *
     * <p>Используется при создании нового тега для проверки уникальности.</p>
     *
     * @param name имя тега; не должно быть {@code null}
     * @return {@code true}, если тег с таким именем существует; {@code false} — иначе
     */
    boolean existsByName(String name);

    /**
     * Возвращает наиболее популярные теги, отсортированные по количеству использований.
     *
     * <p>Используется для отображения «облака тегов» или списка популярных жанров.</p>
     *
     * @return список тегов, отсортированный по убыванию количества использований
     */
    List<Tag> findAllByOrderByUsageCountDesc();

    /**
     * Возвращает все теги, привязанные к указанному контенту.
     *
     * <p>Используется для отображения тегов на странице контента.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return список тегов, привязанных к данному контенту
     */
    @Query("SELECT t FROM Tag t JOIN t.contents c WHERE c.id = :contentId")
    List<Tag> findByContentId(@Param("contentId") Long contentId);

    /**
     * Ищет теги по подстроке в имени (без учёта регистра).
     *
     * <p>Используется для автодополнения при вводе тега.</p>
     *
     * @param name подстрока для поиска; не должна быть {@code null}
     * @return список тегов, содержащих указанную подстроку в имени
     */
    List<Tag> findByNameContainingIgnoreCase(String name);
}
