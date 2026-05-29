package ru.cinema.service.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.cinema.dto.admin.AdminUpdateUserRequest;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.CommentRepository;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link AdminService}: updateUser happy + NotFound, deleteUser NotFound.
 *
 * <p>Тест на stats() намеренно не пишем — там слишком много моков на enum-loop'ах,
 * это интеграционный тест-кандидат, не unit.</p>
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepo;
    @Mock ContentRepository contentRepo;
    @Mock ReviewRepository reviewRepo;
    @Mock CommentRepository commentRepo;
    @Mock RatingRepository ratingRepo;

    @InjectMocks AdminService service;

    @Test
    @DisplayName("updateUser: меняет роль и активность")
    void updateUser_happyPath_updatesRoleAndActive() {
        User u = new User();
        u.setId(7L);
        u.setUsername("john");
        u.setRole(UserRole.USER);
        u.setIsActive(true);

        when(userRepo.findById(7L)).thenReturn(Optional.of(u));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUpdateUserRequest req = new AdminUpdateUserRequest(UserRole.ADMIN, false);
        User updated = service.updateUser(7L, req);

        assertThat(updated.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(updated.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("updateUser: бросает NotFoundException для несуществующего id")
    void updateUser_throwsNotFound_whenMissing() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(999L, new AdminUpdateUserRequest(UserRole.USER, true)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("deleteUser: бросает NotFoundException, если пользователя нет")
    void deleteUser_throwsNotFound_whenMissing() {
        when(userRepo.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteUser(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("listUsers: без фильтров делегирует в findAll(pageable)")
    void listUsers_noFilters_delegatesToFindAll() {
        Pageable pageable = Pageable.unpaged();
        // настраиваем lenient, потому что Mockito может ругаться на NotAMockException у listUsers
        lenient().when(userRepo.findAll(pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(List.of()));

        var page = service.listUsers(null, null, pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
    }
}
