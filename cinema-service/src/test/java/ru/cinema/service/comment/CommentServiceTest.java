package ru.cinema.service.comment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cinema.dto.comment.CommentResponse;
import ru.cinema.dto.comment.CreateCommentRequest;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Comment;
import ru.cinema.model.Content;
import ru.cinema.model.Movie;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.CommentRepository;
import ru.cinema.repository.ContentRepository;
import ru.cinema.service.user.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link CommentService}: create happy, content NotFound.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepo;
    @Mock ContentRepository contentRepo;
    @Mock UserService userService;

    @InjectMocks CommentService service;

    private User user(Long id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setUsername("u" + id);
        u.setRole(role);
        u.setIsActive(true);
        return u;
    }

    private Content content(Long id) {
        Movie m = new Movie();
        m.setId(id);
        m.setTitle("M");
        return m;
    }

    @Test
    @DisplayName("create: успешно сохраняет комментарий")
    void create_happyPath() {
        Content c = content(5L);
        User author = user(1L, UserRole.USER);
        CreateCommentRequest req = new CreateCommentRequest("Отличный фильм!");

        when(contentRepo.findById(5L)).thenReturn(Optional.of(c));
        when(userService.getById(1L)).thenReturn(author);
        when(commentRepo.save(any(Comment.class))).thenAnswer(inv -> {
            Comment cm = inv.getArgument(0);
            cm.setId(33L);
            cm.setCreatedAt(LocalDateTime.now());
            return cm;
        });

        CommentResponse resp = service.create(5L, 1L, req);

        assertThat(resp.id()).isEqualTo(33L);
        assertThat(resp.text()).isEqualTo("Отличный фильм!");
        assertThat(resp.isEdited()).isFalse();
    }

    @Test
    @DisplayName("create: бросает NotFoundException, если контента нет")
    void create_throwsNotFound_whenContentMissing() {
        when(contentRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(999L, 1L, new CreateCommentRequest("text")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Content");

        verify(commentRepo, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("delete: USER не может удалить чужой комментарий (ForbiddenException)")
    void delete_throwsForbidden_whenNotOwnerAndNotAdmin() {
        Comment c = new Comment();
        c.setId(8L);
        c.setUser(user(99L, UserRole.USER));   // автор — другой
        c.setContent(content(5L));
        c.setText("чужой коммент");

        when(commentRepo.findById(8L)).thenReturn(Optional.of(c));
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.USER));

        assertThatThrownBy(() -> service.delete(8L, 1L))
                .isInstanceOf(ForbiddenException.class);

        verify(commentRepo, never()).delete(any(Comment.class));
    }
}
