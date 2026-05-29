package ru.cinema.service.recommendation;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.model.Content;
import ru.cinema.model.Rating;
import ru.cinema.model.Review;
import ru.cinema.model.Tag;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.repository.ReviewRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Тренер и держатель латентной модели рекомендаций.
 *
 * <p>Реализует production-grade pipeline для курсового проекта:</p>
 *
 * <ol>
 *   <li><b>Aggregation</b> — собирает наблюдения из {@link Rating} (явные оценки)
 *       + {@link Review#getRatingValue() Review.ratingValue} (рецензии — это
 *       те же оценки, но с большим вкладом expertise) с
 *       <i>confidence-весом</i> и <i>time-decay</i>: <code>w = base · exp(-age / 365 days)</code>.</li>
 *   <li><b>SVD</b> — обучает {@link MatrixFactorizationModel Funk-SVD} с biases
 *       (Netflix Prize 2006) на этих наблюдениях.</li>
 *   <li><b>TF-IDF теги</b> — параллельно строит content-based TF-IDF матрицу
 *       тегов; редкие теги (например «нуар») дают больший сигнал чем массовые
 *       («драма»). Используется как fallback для cold-start items.</li>
 *   <li><b>Hybrid scoring</b> — финальный скор = α · SVD + β · CB.</li>
 *   <li><b>MMR diversification</b> — re-ranking финального списка через
 *       Maximal Marginal Relevance (Carbonell &amp; Goldstein, 1998), чтобы
 *       не выдавать 10 драм подряд.</li>
 * </ol>
 *
 * <p>Модель пересчитывается по расписанию ({@link Scheduled}) — данные не успевают
 * сильно измениться за 30 минут, но мы получаем sub-millisecond ответы на запросы.</p>
 */
@Component
public class RecommenderEngine {

    private static final Logger log = LoggerFactory.getLogger(RecommenderEngine.class);

    /** Веса гибрида CF и CB в финальном скоре. */
    private static final double WEIGHT_CF = 0.7;
    private static final double WEIGHT_CB = 0.3;

    /** Полураспад влияния старой оценки — год. После 365 дней вес × 1/e ≈ 0.37. */
    private static final double TIME_DECAY_DAYS = 365.0;

    /** Базовый вес наблюдений. Confidence-множители: лайки рецензии добавляют вес. */
    private static final double BASE_WEIGHT_RATING = 1.0;
    private static final double BASE_WEIGHT_REVIEW = 1.5;
    private static final double LIKE_BOOST_PER_LIKE = 0.05; // +5% за каждый лайк рецензии (cap 2.0×)
    private static final double LIKE_BOOST_CAP = 2.0;

    /** MMR balance: 0 = pure score, 1 = pure diversity. */
    private static final double MMR_LAMBDA = 0.7;

    /** Минимум наблюдений у юзера для персональной выдачи (иначе trending). */
    private static final int MIN_OBS_FOR_PERSONAL = 3;

    private final ContentRepository contentRepo;
    private final RatingRepository ratingRepo;
    private final ReviewRepository reviewRepo;

    /** Текущая модель — заменяется атомарно после переобучения. */
    private final AtomicReference<TrainedState> state = new AtomicReference<>(TrainedState.empty());

    public RecommenderEngine(ContentRepository contentRepo,
                             RatingRepository ratingRepo,
                             ReviewRepository reviewRepo) {
        this.contentRepo = contentRepo;
        this.ratingRepo = ratingRepo;
        this.reviewRepo = reviewRepo;
    }

    @PostConstruct
    void initialTrain() {
        try {
            retrain();
        } catch (Exception e) {
            log.warn("Initial recommender training failed (likely empty DB): {}", e.getMessage());
        }
    }

    /** Переобучение по расписанию — каждые 30 минут. Не блокирует читателей. */
    @Scheduled(fixedDelay = 30 * 60 * 1000L, initialDelay = 30 * 60 * 1000L)
    @Transactional(readOnly = true)
    public void retrain() {
        long t0 = System.currentTimeMillis();
        List<Content> catalog = contentRepo.findAllPublishedWithTags(ContentStatus.PUBLISHED);
        List<Rating> ratings = ratingRepo.findAllRatings();
        List<Review> reviews = reviewRepo.findAllPublishedWithRating(ReviewStatus.PUBLISHED);

        TrainedState fresh = buildState(catalog, ratings, reviews);
        state.set(fresh);

        log.info("Recommender retrained in {} ms — {} obs, {} users, {} items, μ={}",
                System.currentTimeMillis() - t0,
                fresh.observationCount,
                fresh.model.userCount(), fresh.model.itemCount(),
                String.format("%.2f", fresh.model.globalMean()));
    }

    private TrainedState buildState(List<Content> catalog, List<Rating> ratings, List<Review> reviews) {
        // ---- Aggregate observations ----
        // Один и тот же (user, item) может попасться дважды: явный rating + review.
        // Берём максимальный rating и суммируем confidence — пользователь дал
        // и оценку, и рецензию = более уверенное наблюдение.
        Map<Long, Map<Long, double[]>> agg = new HashMap<>();   // userId -> itemId -> [maxRating, sumConfidence]
        LocalDateTime now = LocalDateTime.now();

        for (Rating r : ratings) {
            if (r.getValue() == null) continue;
            double w = BASE_WEIGHT_RATING * timeDecay(r.getCreatedAt(), now);
            mergeObservation(agg, r.getUser().getId(), r.getContent().getId(), r.getValue(), w);
        }
        for (Review r : reviews) {
            if (r.getRatingValue() == null) continue;
            double likeBoost = Math.min(LIKE_BOOST_CAP,
                    1.0 + LIKE_BOOST_PER_LIKE * (r.getLikeCount() == null ? 0 : r.getLikeCount()));
            double w = BASE_WEIGHT_REVIEW * likeBoost * timeDecay(r.getCreatedAt(), now);
            mergeObservation(agg, r.getUser().getId(), r.getContent().getId(), r.getRatingValue(), w);
        }

        List<MatrixFactorizationModel.Observation> obs = new ArrayList<>();
        Map<Long, Integer> obsCountPerUser = new HashMap<>();
        Map<Long, Set<Long>> observedItemsByUser = new HashMap<>();
        for (var u : agg.entrySet()) {
            Set<Long> userObserved = new HashSet<>();
            for (var i : u.getValue().entrySet()) {
                obs.add(new MatrixFactorizationModel.Observation(
                        u.getKey(), i.getKey(), i.getValue()[0], i.getValue()[1]));
                obsCountPerUser.merge(u.getKey(), 1, Integer::sum);
                userObserved.add(i.getKey());
            }
            observedItemsByUser.put(u.getKey(), userObserved);
        }

        // ---- Train SVD ----
        MatrixFactorizationModel model = MatrixFactorizationModel.train(
                obs, MatrixFactorizationModel.HyperParams.defaults());

        // ---- Build TF-IDF tag index for content-based fallback ----
        TagTfIdf tagIndex = TagTfIdf.build(catalog);

        // ---- Popularity & trending precompute ----
        Map<Long, Long> ratingsCount = ratings.stream()
                .collect(Collectors.groupingBy(r -> r.getContent().getId(), Collectors.counting()));

        Map<Long, Content> byId = catalog.stream()
                .collect(Collectors.toMap(Content::getId, c -> c));

        return new TrainedState(model, tagIndex, byId, ratingsCount,
                obsCountPerUser, observedItemsByUser, obs.size());
    }

    private static void mergeObservation(Map<Long, Map<Long, double[]>> agg,
                                         long userId, long itemId,
                                         double rating, double weight) {
        agg.computeIfAbsent(userId, k -> new HashMap<>())
                .merge(itemId, new double[]{rating, weight}, (oldV, newV) -> {
                    oldV[0] = Math.max(oldV[0], newV[0]);  // max rating
                    oldV[1] += newV[1];                     // sum confidence
                    return oldV;
                });
    }

    private static double timeDecay(LocalDateTime created, LocalDateTime now) {
        if (created == null) return 0.5;  // unknown age — half weight
        double days = Duration.between(created, now).toDays();
        return Math.exp(-days / TIME_DECAY_DAYS);
    }

    // =================================================================
    // Чтение модели — потокобезопасно, lock-free
    // =================================================================

    public TrainedState snapshot() {
        return state.get();
    }

    /**
     * Скорит контент для пользователя — гибрид CF + CB. Возвращает [0..1] нормализованно.
     * Если модель не знает юзера — fallback на чистый CB.
     */
    public ScoredCandidate scoreForUser(Long userId, Content c, Map<Long, Double> userTagAffinity) {
        TrainedState s = state.get();

        // CF: SVD prediction нормализуем 1..5 → [0..1]
        double cf = 0;
        Double cfRaw = (userId != null && s.model.knowsUser(userId)) ? s.model.predict(userId, c.getId()) : null;
        if (cfRaw != null) cf = clamp01((cfRaw - 1.0) / 4.0);

        // CB: cosine между TF-IDF юзера и TF-IDF контента
        double cb = userTagAffinity.isEmpty() ? 0 : s.tagIndex.cosineForUser(userTagAffinity, c);

        double total = (cfRaw != null)
                ? WEIGHT_CF * cf + WEIGHT_CB * cb
                : cb;  // нет CF — только content-based

        String reason = buildReason(c, cfRaw, cb, userTagAffinity);
        return new ScoredCandidate(c, total, reason);
    }

    /** Item-item similarity — приоритет SVD (вкусовое сходство), fallback на TF-IDF тегов. */
    public ScoredCandidate scoreSimilar(Content target, Content other) {
        TrainedState s = state.get();
        Double svdSim = s.model.itemSimilarity(target.getId(), other.getId());
        double tagSim = s.tagIndex.itemCosine(target, other);

        double score;
        String reason;
        String tagNames = intersectionTagNames(target, other, 3);
        if (svdSim != null && svdSim > 0) {
            score = 0.6 * svdSim + 0.4 * tagSim;
            reason = tagNames.isEmpty()
                    ? "Похож по вкусам аудитории"
                    : "Похож по вкусам и тегам: " + tagNames;
        } else if (tagSim > 0) {
            score = tagSim;
            reason = "Похож по тегам: " + tagNames;
        } else {
            score = 0;
            reason = "";
        }
        return new ScoredCandidate(other, score, reason);
    }

    /**
     * MMR re-ranking — Maximal Marginal Relevance.
     * Carbonell & Goldstein 1998: score = λ·relevance - (1-λ)·max_similarity_to_already_picked.
     * Убирает «9 драм подряд» в выдаче.
     */
    public List<ScoredCandidate> diversify(List<ScoredCandidate> candidates, int limit) {
        if (candidates.size() <= limit) return candidates;
        TrainedState s = state.get();
        List<ScoredCandidate> picked = new ArrayList<>();
        List<ScoredCandidate> pool = new ArrayList<>(candidates);

        while (picked.size() < limit && !pool.isEmpty()) {
            ScoredCandidate best = null;
            double bestMmr = Double.NEGATIVE_INFINITY;
            int bestIdx = -1;

            for (int i = 0; i < pool.size(); i++) {
                ScoredCandidate cand = pool.get(i);
                double maxSimToPicked = 0;
                for (ScoredCandidate p : picked) {
                    Double svdSim = s.model.itemSimilarity(cand.content.getId(), p.content.getId());
                    double tagSim = s.tagIndex.itemCosine(cand.content, p.content);
                    double sim = svdSim != null ? Math.max(svdSim, tagSim) : tagSim;
                    if (sim > maxSimToPicked) maxSimToPicked = sim;
                }
                double mmr = MMR_LAMBDA * cand.score - (1 - MMR_LAMBDA) * maxSimToPicked;
                if (mmr > bestMmr) {
                    bestMmr = mmr;
                    best = cand;
                    bestIdx = i;
                }
            }
            picked.add(best);
            pool.remove(bestIdx);
        }
        return picked;
    }

    /** Считает TF-IDF affinity юзера по его наблюдениям с весом по оценке. */
    public Map<Long, Double> userTagAffinity(Long userId) {
        if (userId == null) return Map.of();
        TrainedState s = state.get();
        Map<Long, Double> affinity = new HashMap<>();
        Map<Long, Double> norm = new HashMap<>();

        // Берём наблюдения юзера — те же что были в обучении
        // Используем ratings + reviews напрямую через SVD-обученные factor — но
        // для CB-вектора лучше явно отфильтровать положительные оценки.
        // Здесь делаем query повторно для актуальности.
        List<Rating> userRatings = ratingRepo.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        for (Rating r : userRatings) {
            // на шкале 1–5 «нравится» начинается с 4
            if (r.getValue() == null || r.getValue() < 4) continue;
            Content c = s.byId.get(r.getContent().getId());
            if (c == null || c.getTags() == null) continue;
            double w = (r.getValue() / 5.0) * timeDecay(r.getCreatedAt(), now);
            for (Tag t : c.getTags()) {
                double idf = s.tagIndex.idf(t.getId());
                affinity.merge(t.getId(), w * idf, Double::sum);
                norm.merge(t.getId(), 1.0, Double::sum);
            }
        }
        // нормализация — среднее, чтобы юзер с 50 оценками не доминировал
        for (var e : affinity.entrySet()) {
            double n = norm.getOrDefault(e.getKey(), 1.0);
            e.setValue(e.getValue() / n);
        }
        return affinity;
    }

    public int observationCount(long userId) {
        return state.get().obsCountPerUser.getOrDefault(userId, 0);
    }

    public boolean hasEnoughObservations(Long userId) {
        return userId != null && observationCount(userId) >= MIN_OBS_FOR_PERSONAL;
    }

    public List<Content> publishedCatalog() {
        return new ArrayList<>(state.get().byId.values());
    }

    public Map<Long, Long> ratingsCountIndex() {
        return state.get().ratingsCount;
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }

    private static String intersectionTagNames(Content a, Content b, int n) {
        if (a.getTags() == null || b.getTags() == null) return "";
        Set<Long> bIds = b.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        return a.getTags().stream()
                .filter(t -> bIds.contains(t.getId()))
                .limit(n)
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
    }

    private static String buildReason(Content c, Double cfRaw, double cb, Map<Long, Double> affinity) {
        if (cfRaw != null && cfRaw > 6 && cb > 0.05) {
            return "Похожие пользователи дают " + String.format("%.1f", cfRaw)
                    + "; " + topTags(c, affinity, 2);
        }
        if (cfRaw != null && cfRaw > 6) {
            return "Похожие пользователи оценили на " + String.format("%.1f", cfRaw);
        }
        if (cb > 0.05) {
            return "Совпадает по тегам: " + topTags(c, affinity, 3);
        }
        return "Trending в каталоге";
    }

    private static String topTags(Content c, Map<Long, Double> affinity, int n) {
        if (c.getTags() == null) return "";
        return c.getTags().stream()
                .filter(t -> affinity.containsKey(t.getId()))
                .sorted((x, y) -> Double.compare(
                        affinity.getOrDefault(y.getId(), 0.0),
                        affinity.getOrDefault(x.getId(), 0.0)))
                .limit(n)
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
    }

    /** Кандидат для ранжирования. */
    public record ScoredCandidate(Content content, double score, String reason) {}

    /** Иммутабельный снимок обученного состояния. */
    public record TrainedState(
            MatrixFactorizationModel model,
            TagTfIdf tagIndex,
            Map<Long, Content> byId,
            Map<Long, Long> ratingsCount,
            Map<Long, Integer> obsCountPerUser,
            Map<Long, Set<Long>> observedItemsByUser,
            int observationCount
    ) {
        static TrainedState empty() {
            return new TrainedState(
                    MatrixFactorizationModel.train(List.of(),
                            MatrixFactorizationModel.HyperParams.defaults()),
                    TagTfIdf.build(List.of()),
                    Map.of(), Map.of(), Map.of(), Map.of(), 0);
        }
    }
}
