package ru.cinema.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cinema.dto.recommendation.RecommendationResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.recommendation.RecommendationService;

import java.util.List;

/**
 * REST API персонализированных и популярных рекомендаций.
 *
 * <ul>
 *   <li>{@code GET /api/v1/recommendations/for-me} — гибрид CB+CF для авторизованного,
 *       trending fallback при недостатке оценок, guest-выборка для анонимного.</li>
 *   <li>{@code GET /api/v1/recommendations/similar/{id}} — content-based по тегам.</li>
 *   <li>{@code GET /api/v1/recommendations/trending} — глобальный trending по композитной метрике.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "Персональные и популярные рекомендации")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/for-me")
    @Operation(summary = "Персональные рекомендации (hybrid CB + CF)",
            description = "Если пользователь не залогинен — отдаёт guest-выборку. " +
                    "Если у пользователя меньше 3 оценок — fallback на trending. " +
                    "Иначе — content-based + collaborative filtering, веса 0.6 / 0.4.")
    public List<RecommendationResponse> forMe(@RequestParam(defaultValue = "12") int limit) {
        Long uid = CurrentUser.currentUserId();
        if (uid == null) {
            return recommendationService.forGuest(limit);
        }
        return recommendationService.recommendForUser(uid, limit);
    }

    @GetMapping("/similar/{contentId}")
    @Operation(summary = "Похожий контент (content-based по тегам)")
    public List<RecommendationResponse> similar(@PathVariable Long contentId,
                                                @RequestParam(defaultValue = "8") int limit) {
        return recommendationService.findSimilar(contentId, limit);
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending (rating + log popularity + recency decay)")
    public List<RecommendationResponse> trending(@RequestParam(defaultValue = "20") int limit) {
        return recommendationService.trending(limit);
    }
}
