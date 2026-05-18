package ru.cinema.service.recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Funk-SVD matrix factorization (Simon Funk, Netflix Prize 2006).
 *
 * <p>Модель предсказывает оценку как сумму глобального среднего, смещений
 * пользователя/контента и скалярного произведения латентных факторов:</p>
 *
 * <pre>
 *   r̂_ui = μ + b_u + b_i + p_u · q_i
 * </pre>
 *
 * <p>Параметры обучаются стохастическим градиентным спуском с L2-регуляризацией.
 * Каждое наблюдение {@link Observation} может нести индивидуальный вес — это
 * позволяет учитывать implicit-feedback (лайки, комментарии, time-decay)
 * без изменения целевой функции.</p>
 *
 * <p><b>Ссылки:</b></p>
 * <ul>
 *   <li>Funk, S. «Netflix Update: Try This at Home» (2006).</li>
 *   <li>Koren, Y. «Factor in the Neighbors: Scalable and Accurate Collaborative
 *       Filtering» (2010) — обоснование biases-расширения.</li>
 *   <li>Hu, Koren, Volinsky «Collaborative Filtering for Implicit Feedback
 *       Datasets» (2008) — confidence-weighted loss.</li>
 * </ul>
 *
 * <p>Модель неизменяема после обучения; thread-safe для чтения.</p>
 */
public final class MatrixFactorizationModel {

    /** Единица обучающей выборки: оценка пользователя за контент с весом доверия. */
    public record Observation(long userId, long itemId, double rating, double confidence) {}

    /** Гиперпараметры модели — подобраны для курсового объёма (десятки-сотни оценок). */
    public record HyperParams(
            int factors,            // K — размерность латентного пространства
            int iterations,         // эпохи SGD
            double learningRate,    // γ
            double regularization,  // λ
            double initScale,       // амплитуда инициализации факторов
            long randomSeed
    ) {
        public static HyperParams defaults() {
            return new HyperParams(20, 30, 0.005, 0.05, 0.1, 42L);
        }
    }

    private final HyperParams params;
    private final double globalMean;
    private final Map<Long, Integer> userIndex;
    private final Map<Long, Integer> itemIndex;
    private final long[] userIds;
    private final long[] itemIds;
    private final double[] userBias;
    private final double[] itemBias;
    private final double[][] userFactors;   // [nUsers][K]
    private final double[][] itemFactors;   // [nItems][K]
    private final double[] itemNorms;        // ||q_i|| — для cosine между айтемами

    private MatrixFactorizationModel(HyperParams params,
                                     double globalMean,
                                     Map<Long, Integer> userIndex,
                                     Map<Long, Integer> itemIndex,
                                     long[] userIds,
                                     long[] itemIds,
                                     double[] userBias,
                                     double[] itemBias,
                                     double[][] userFactors,
                                     double[][] itemFactors,
                                     double[] itemNorms) {
        this.params = params;
        this.globalMean = globalMean;
        this.userIndex = userIndex;
        this.itemIndex = itemIndex;
        this.userIds = userIds;
        this.itemIds = itemIds;
        this.userBias = userBias;
        this.itemBias = itemBias;
        this.userFactors = userFactors;
        this.itemFactors = itemFactors;
        this.itemNorms = itemNorms;
    }

    /**
     * Тренирует модель на наблюдениях. Возвращает иммутабельный экземпляр —
     * параметры зафиксированы, модель готова к чтению из любых потоков.
     */
    public static MatrixFactorizationModel train(List<Observation> observations, HyperParams hp) {
        if (observations.isEmpty()) {
            return empty(hp);
        }

        // 1. Индексация — переводим Long-id в плотные [0..N) индексы
        Map<Long, Integer> userIndex = new HashMap<>();
        Map<Long, Integer> itemIndex = new HashMap<>();
        for (Observation o : observations) {
            userIndex.computeIfAbsent(o.userId, k -> userIndex.size());
            itemIndex.computeIfAbsent(o.itemId, k -> itemIndex.size());
        }
        long[] userIds = new long[userIndex.size()];
        long[] itemIds = new long[itemIndex.size()];
        userIndex.forEach((id, idx) -> userIds[idx] = id);
        itemIndex.forEach((id, idx) -> itemIds[idx] = id);

        int nUsers = userIds.length;
        int nItems = itemIds.length;
        int k = hp.factors();

        // 2. Глобальное среднее (взвешенное по confidence)
        double sumW = 0, sumWR = 0;
        for (Observation o : observations) {
            sumW  += o.confidence;
            sumWR += o.confidence * o.rating;
        }
        double mu = sumW > 0 ? sumWR / sumW : 0;

        // 3. Инициализация факторов
        Random rng = new Random(hp.randomSeed());
        double[] bU = new double[nUsers];
        double[] bI = new double[nItems];
        double[][] P = new double[nUsers][k];
        double[][] Q = new double[nItems][k];
        for (int u = 0; u < nUsers; u++) for (int f = 0; f < k; f++) P[u][f] = (rng.nextDouble() - 0.5) * hp.initScale();
        for (int i = 0; i < nItems; i++) for (int f = 0; f < k; f++) Q[i][f] = (rng.nextDouble() - 0.5) * hp.initScale();

        double gamma = hp.learningRate();
        double lambda = hp.regularization();

        // 4. SGD по эпохам — на каждой проходим observations в случайном порядке
        int[] order = new int[observations.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;

        for (int epoch = 0; epoch < hp.iterations(); epoch++) {
            shuffle(order, rng);
            for (int idx : order) {
                Observation o = observations.get(idx);
                int u = userIndex.get(o.userId);
                int i = itemIndex.get(o.itemId);
                double w = o.confidence;

                double dot = 0;
                for (int f = 0; f < k; f++) dot += P[u][f] * Q[i][f];
                double pred = mu + bU[u] + bI[i] + dot;
                double err = w * (o.rating - pred);

                // Обновления (Funk-SVD с biases): движемся вдоль antigradient
                bU[u] += gamma * (err - lambda * bU[u]);
                bI[i] += gamma * (err - lambda * bI[i]);
                for (int f = 0; f < k; f++) {
                    double pf = P[u][f];
                    double qf = Q[i][f];
                    P[u][f] += gamma * (err * qf - lambda * pf);
                    Q[i][f] += gamma * (err * pf - lambda * qf);
                }
            }
        }

        // 5. Pre-compute item norms for cosine similarity
        double[] itemNorms = new double[nItems];
        for (int i = 0; i < nItems; i++) {
            double n = 0;
            for (int f = 0; f < k; f++) n += Q[i][f] * Q[i][f];
            itemNorms[i] = Math.sqrt(n);
        }

        return new MatrixFactorizationModel(hp, mu, userIndex, itemIndex,
                userIds, itemIds, bU, bI, P, Q, itemNorms);
    }

    private static MatrixFactorizationModel empty(HyperParams hp) {
        return new MatrixFactorizationModel(hp, 0, Map.of(), Map.of(),
                new long[0], new long[0], new double[0], new double[0],
                new double[0][hp.factors()], new double[0][hp.factors()], new double[0]);
    }

    /** Предсказывает рейтинг (1..10) пользователя для контента; null если параметры не выучены. */
    public Double predict(long userId, long itemId) {
        Integer u = userIndex.get(userId);
        Integer i = itemIndex.get(itemId);
        if (u == null || i == null) return null;
        return globalMean + userBias[u] + itemBias[i] + dot(userFactors[u], itemFactors[i]);
    }

    /**
     * Косинус латентных q-векторов двух items — основа item-item similarity.
     * Учитывает «вкусовое сходство» а не только пересечение тегов.
     */
    public Double itemSimilarity(long itemA, long itemB) {
        Integer a = itemIndex.get(itemA);
        Integer b = itemIndex.get(itemB);
        if (a == null || b == null) return null;
        double na = itemNorms[a], nb = itemNorms[b];
        if (na == 0 || nb == 0) return 0.0;
        return dot(itemFactors[a], itemFactors[b]) / (na * nb);
    }

    public boolean knowsUser(long userId) { return userIndex.containsKey(userId); }
    public boolean knowsItem(long itemId) { return itemIndex.containsKey(itemId); }
    public int userCount() { return userIds.length; }
    public int itemCount() { return itemIds.length; }
    public double globalMean() { return globalMean; }
    public HyperParams hyperParams() { return params; }

    public long[] knownItemIds() { return itemIds.clone(); }

    // ===== math helpers =====

    private static double dot(double[] a, double[] b) {
        double s = 0;
        for (int f = 0; f < a.length; f++) s += a[f] * b[f];
        return s;
    }

    private static void shuffle(int[] arr, Random r) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = arr[i]; arr[i] = arr[j]; arr[j] = t;
        }
    }
}
