package ru.cinema.service.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.dto.recommendation.RecommendationResponse;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.enums.ContentType;
import ru.cinema.repository.ContentRepository;
import ru.cinema.service.recommendation.RecommenderEngine.ScoredCandidate;

import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Фасад над {@link RecommenderEngine}.
 *
 * <p>Контроллеры зовут именно этот сервис. Реальная математика
 * (Funk-SVD + TF-IDF + MMR) живёт в {@link RecommenderEngine}; здесь —
 * только сценарии: «для меня», «похожее», «trending», «гостю». Каждый сценарий
 * умеет делать fallback (cold-start, недостаточно данных) на более простую
 * метрику без падений.</p>
 */
@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RecommenderEngine engine;
    private final ContentRepository contentRepo;

    public RecommendationService(RecommenderEngine engine, ContentRepository contentRepo) {
        this.engine = engine;
        this.contentRepo = contentRepo;
    }

    // =================================================================
    // 1. Personal recommendations (CF + CB hybrid + MMR)
    // =================================================================

    public List<RecommendationResponse> recommendForUser(Long userId, int limit) {
        if (userId == null) return forGuest(limit);
        if (!engine.hasEnoughObservations(userId)) {
            log.debug("user {} has too few observations, fallback to trending", userId);
            return trending(limit);
        }

        Map<Long, Double> affinity = engine.userTagAffinity(userId);
        List<Content> catalog = engine.publishedCatalog();
        var snapshot = engine.snapshot();
        var observed = snapshot.observedItemsByUser().getOrDefault(userId, java.util.Set.of());

        List<ScoredCandidate> scored = new ArrayList<>();
        for (Content c : catalog) {
            if (observed.contains(c.getId())) continue;       // не рекомендуем уже оценённое
            ScoredCandidate sc = engine.scoreForUser(userId, c, affinity);
            if (sc.score() > 0) scored.add(sc);
        }

        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        List<ScoredCandidate> top = scored.size() > limit * 3
                ? scored.subList(0, limit * 3)
                : scored;
        return toResponse(engine.diversify(top, limit));
    }

    // =================================================================
    // 2. Similar content (item-item via SVD embeddings + TF-IDF fallback)
    // =================================================================

    public List<RecommendationResponse> findSimilar(Long contentId, int limit) {
        Content target = contentRepo.findById(contentId)
                .orElseThrow(() -> NotFoundException.of("Content", contentId));

        List<Content> catalog = engine.publishedCatalog();
        List<ScoredCandidate> scored = new ArrayList<>();
        for (Content c : catalog) {
            if (c.getId().equals(target.getId())) continue;
            ScoredCandidate sc = engine.scoreSimilar(target, c);
            if (sc.score() > 0) scored.add(sc);
        }
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        List<ScoredCandidate> top = scored.size() > limit * 3
                ? scored.subList(0, limit * 3)
                : scored;
        return toResponse(engine.diversify(top, limit));
    }

    // =================================================================
    // 3. Trending — composite (rating + log popularity + recency)
    // =================================================================

    public List<RecommendationResponse> trending(int limit) {
        List<Content> catalog = engine.publishedCatalog();
        if (catalog.isEmpty()) return List.of();
        Map<Long, Long> ratingsCount = engine.ratingsCountIndex();

        double maxRating = catalog.stream().mapToDouble(this::ratingOf).max().orElse(1);
        double minRating = catalog.stream().mapToDouble(this::ratingOf).min().orElse(0);
        double maxLogCnt = catalog.stream()
                .mapToDouble(c -> Math.log1p(ratingsCount.getOrDefault(c.getId(), 0L)))
                .max().orElse(1);
        int currentYear = Year.now().getValue();

        List<ScoredCandidate> scored = new ArrayList<>();
        for (Content c : catalog) {
            double rating = ratingOf(c);
            double normR = (maxRating - minRating) == 0 ? 0 : (rating - minRating) / (maxRating - minRating);
            long cnt = ratingsCount.getOrDefault(c.getId(), 0L);
            double normPop = maxLogCnt == 0 ? 0 : Math.log1p(cnt) / maxLogCnt;
            double recency = c.getReleaseYear() == null ? 0
                    : Math.exp(-(double) (currentYear - c.getReleaseYear()) / 10.0);
            double score = 0.5 * normR + 0.3 * normPop + 0.2 * recency;
            scored.add(new ScoredCandidate(c, score, "Trending в каталоге"));
        }
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        List<ScoredCandidate> top = scored.size() > limit * 2
                ? scored.subList(0, limit * 2)
                : scored;
        return toResponse(engine.diversify(top, limit));
    }

    // =================================================================
    // 4. Guest recommendations
    // =================================================================

    public List<RecommendationResponse> forGuest(int limit) {
        List<Content> catalog = engine.publishedCatalog();
        Map<Long, Long> ratingsCount = engine.ratingsCountIndex();

        Comparator<Content> byPopularity = Comparator.<Content>comparingDouble(c -> {
            double r = ratingOf(c);
            long cnt = ratingsCount.getOrDefault(c.getId(), 0L);
            return r * Math.log1p(cnt + 1);
        }).reversed();

        List<Content> movies = catalog.stream()
                .filter(c -> c.getContentType() == ContentType.MOVIE)
                .sorted(byPopularity).toList();
        List<Content> series = catalog.stream()
                .filter(c -> c.getContentType() == ContentType.SERIES)
                .sorted(byPopularity).toList();

        int half = Math.max(1, limit / 2);
        List<Content> picked = new ArrayList<>();
        picked.addAll(movies.stream().limit(half).toList());
        picked.addAll(series.stream().limit(limit - picked.size()).toList());
        if (picked.size() < limit) {
            movies.stream().skip(half).limit(limit - picked.size()).forEach(picked::add);
        }

        return picked.stream()
                .map(c -> RecommendationResponse.of(ContentListItem.of(c),
                        ratingOf(c),
                        "Популярное на MovieHub"))
                .toList();
    }

    private double ratingOf(Content c) {
        return c.getAverageRating() == null ? 0 : c.getAverageRating().doubleValue();
    }

    private List<RecommendationResponse> toResponse(List<ScoredCandidate> scored) {
        return scored.stream()
                .map(s -> RecommendationResponse.of(
                        ContentListItem.of(s.content()),
                        round3(s.score()),
                        s.reason()))
                .toList();
    }

    private static double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
