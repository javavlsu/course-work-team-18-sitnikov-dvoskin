package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Компонент доступа к данным для сущности {@link User}.
 *
 * <p><b>Назначение:</b> предоставляет методы для выполнения CRUD-операций
 * и специализированных запросов к таблице пользователей.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Регистрация — создание нового пользователя</li>
 *     <li>Управление профилем — поиск и обновление данных пользователя</li>
 *     <li>Блокировка пользователя (администратор) — деактивация учётной записи</li>
 *     <li>Аналитика системы — подсчёт и выборка пользователей</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени (логину).
     *
     * <p>Используется при аутентификации и проверке уникальности логина при регистрации.</p>
     *
     * @param username имя пользователя (логин); не должно быть {@code null} или пустым
     * @return {@link Optional} с найденным пользователем, или пустой {@link Optional},
     *         если пользователь с таким именем не существует
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по адресу электронной почты.
     *
     * <p>Используется при регистрации для проверки уникальности email
     * и при восстановлении пароля.</p>
     *
     * @param email адрес электронной почты; не должен быть {@code null} или пустым
     * @return {@link Optional} с найденным пользователем, или пустой {@link Optional},
     *         если пользователь с таким email не существует
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет, существует ли пользователь с указанным именем.
     *
     * <p>Используется для быстрой валидации при регистрации без загрузки всей сущности.</p>
     *
     * @param username имя пользователя для проверки; не должно быть {@code null}
     * @return {@code true}, если пользователь с таким именем существует; {@code false} — иначе
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет, существует ли пользователь с указанным email.
     *
     * <p>Используется для быстрой валидации при регистрации без загрузки всей сущности.</p>
     *
     * @param email адрес электронной почты для проверки; не должен быть {@code null}
     * @return {@code true}, если пользователь с таким email существует; {@code false} — иначе
     */
    boolean existsByEmail(String email);

    /**
     * Возвращает список пользователей с указанной ролью.
     *
     * <p>Используется в административной панели для фильтрации пользователей по ролям.</p>
     *
     * @param role роль пользователя ({@link UserRole}); не должна быть {@code null}
     * @return список пользователей с указанной ролью; пустой список, если таких нет
     */
    List<User> findByRole(UserRole role);

    /**
     * Возвращает список активных/неактивных пользователей.
     *
     * <p>Используется администратором для просмотра заблокированных аккаунтов
     * ({@code isActive = false}) или активных пользователей ({@code isActive = true}).</p>
     *
     * @param isActive статус активности: {@code true} — активные, {@code false} — заблокированные
     * @return список пользователей с указанным статусом активности
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Возвращает количество пользователей, зарегистрированных после указанной даты.
     *
     * <p>Используется в аналитике системы для отслеживания динамики регистраций.</p>
     *
     * @param date дата, начиная с которой считать новых пользователей; не должна быть {@code null}
     * @return количество пользователей, зарегистрированных после указанной даты
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersRegisteredAfter(@Param("date") LocalDateTime date);
}
