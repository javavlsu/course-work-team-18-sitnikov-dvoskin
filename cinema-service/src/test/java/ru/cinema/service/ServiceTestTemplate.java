package ru.cinema.service;

/**
 * <h2>Шаблон unit-теста для будущих *Service классов.</h2>
 *
 * <p>На момент Этапа 9 (03.05.2026) сервисный слой ещё пишется параллельной командой
 * (см. задачу #2 — Этап 7). Этот файл-инструкция для qa-1: после слияния service-веток
 * скопировать шаблон в файл вида {@code <Service>Test.java} рядом и реализовать тесты.</p>
 *
 * <p><b>Список сервисов под покрытие:</b></p>
 * <ul>
 *   <li>AuthService, UserService — auth/users пакет</li>
 *   <li>ContentService, MovieService, SeriesService, TagService — content/tag пакет</li>
 *   <li>ReviewService, CommentService, RatingService — review/comment/rating пакет</li>
 *   <li>PlaylistService — playlist пакет</li>
 *   <li>AdminService — admin пакет</li>
 * </ul>
 *
 * <p><b>Минимум на каждый сервис:</b></p>
 * <ol>
 *   <li>1 happy-path (например, создание/чтение/обновление)</li>
 *   <li>1 негативный (NotFoundException, ConflictException, валидация)</li>
 * </ol>
 *
 * <p><b>Шаблон:</b></p>
 *
 * <pre>{@code
 * @ExtendWith(MockitoExtension.class)
 * class ContentServiceTest {
 *
 *     @Mock ContentRepository contentRepository;
 *     @Mock TagRepository tagRepository;
 *     @Mock RatingRepository ratingRepository;
 *
 *     @InjectMocks ContentService service;
 *
 *     @Test
 *     void getById_returnsContent_whenExists() {
 *         Content content = new Content();
 *         content.setId(1L);
 *         content.setTitle("Дюна");
 *         when(contentRepository.findById(1L)).thenReturn(Optional.of(content));
 *
 *         var result = service.getById(1L);
 *
 *         assertThat(result.id()).isEqualTo(1L);
 *         assertThat(result.title()).isEqualTo("Дюна");
 *     }
 *
 *     @Test
 *     void getById_throwsNotFound_whenMissing() {
 *         when(contentRepository.findById(999L)).thenReturn(Optional.empty());
 *
 *         assertThatThrownBy(() -> service.getById(999L))
 *             .isInstanceOf(NotFoundException.class);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Запуск:</b> {@code docker compose exec -T app mvn -B test}</p>
 *
 * <p><b>NB:</b> класс не содержит ни одного {@code @Test} сознательно — это документ-шаблон.
 * Maven-Surefire просто проигнорирует его без падения.</p>
 */
public final class ServiceTestTemplate {
    private ServiceTestTemplate() {
        // utility/документация — не для инстанцирования
    }
}
