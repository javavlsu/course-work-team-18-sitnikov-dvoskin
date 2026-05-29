package ru.cinema.dto.recommendation;

import ru.cinema.dto.content.ContentListItem;

/**
 * Ответ алгоритма рекомендаций: единица контента + score + объяснение причины.
 *
 * <p><b>score</b> — нормализованная оценка релевантности (диапазон зависит от метода:
 * для CB / similar — приблизительно [0..1]; для trending — нормализованный композит).</p>
 *
 * <p><b>reason</b> — человекочитаемое объяснение для UI («совпадает по тегам: Драма, Триллер»;
 * «похожие пользователи оценили на 8.5/10»; «trending»).</p>
 */
public record RecommendationResponse(
        ContentListItem content,
        double score,
        String reason
) {
    public static RecommendationResponse of(ContentListItem content, double score, String reason) {
        return new RecommendationResponse(content, score, reason);
    }
}
