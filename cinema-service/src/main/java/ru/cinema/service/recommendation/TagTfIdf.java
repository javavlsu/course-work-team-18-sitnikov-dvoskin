package ru.cinema.service.recommendation;

import ru.cinema.model.Content;
import ru.cinema.model.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TF-IDF индекс тегов для content-based рекомендаций.
 *
 * <p>Мотивация: бинарное «есть тег / нет тега» переоценивает массовые жанры
 * (драма у половины каталога) и недооценивает редкие (нуар у 1%). TF-IDF
 * взвешивает теги по обратной частоте — редкие сигналы сильнее.</p>
 *
 * <pre>
 *   idf(t) = log( (N + 1) / (df(t) + 1) ) + 1
 * </pre>
 *
 * <p>(сглаженная форма из scikit-learn, чтобы избежать деления на ноль для
 * не встречавшихся тегов).</p>
 *
 * <p>Сходство двух items — косинус их IDF-взвешенных tag-векторов.</p>
 */
public final class TagTfIdf {

    private final Map<Long, Double> idf;     // tagId -> idf

    private TagTfIdf(Map<Long, Double> idf) {
        this.idf = idf;
    }

    public static TagTfIdf build(List<Content> catalog) {
        Map<Long, Integer> df = new HashMap<>();
        int n = 0;
        for (Content c : catalog) {
            if (c.getTags() == null || c.getTags().isEmpty()) continue;
            n++;
            Set<Long> tagSet = new HashSet<>();
            for (Tag t : c.getTags()) tagSet.add(t.getId());
            for (Long tid : tagSet) df.merge(tid, 1, Integer::sum);
        }
        Map<Long, Double> idf = new HashMap<>();
        if (n == 0) return new TagTfIdf(idf);
        for (var e : df.entrySet()) {
            idf.put(e.getKey(), Math.log((double) (n + 1) / (e.getValue() + 1)) + 1.0);
        }
        return new TagTfIdf(idf);
    }

    public double idf(Long tagId) {
        return idf.getOrDefault(tagId, 1.0);
    }

    /** Cosine двух items по IDF-взвешенным векторам тегов. */
    public double itemCosine(Content a, Content b) {
        if (a == null || b == null || a.getTags() == null || b.getTags() == null) return 0;
        Map<Long, Double> va = vectorize(a);
        Map<Long, Double> vb = vectorize(b);
        return cosine(va, vb);
    }

    /** Cosine между affinity-вектором юзера (уже IDF-взвешенным) и тегами контента. */
    public double cosineForUser(Map<Long, Double> userAffinity, Content c) {
        if (userAffinity.isEmpty() || c.getTags() == null || c.getTags().isEmpty()) return 0;
        Map<Long, Double> contentVec = vectorize(c);
        return cosine(userAffinity, contentVec);
    }

    private Map<Long, Double> vectorize(Content c) {
        Map<Long, Double> v = new HashMap<>();
        for (Tag t : c.getTags()) {
            v.merge(t.getId(), idf(t.getId()), Double::sum);
        }
        return v;
    }

    private static double cosine(Map<Long, Double> a, Map<Long, Double> b) {
        double dot = 0, na = 0, nb = 0;
        for (var e : a.entrySet()) {
            double av = e.getValue();
            na += av * av;
            Double bv = b.get(e.getKey());
            if (bv != null) dot += av * bv;
        }
        for (var e : b.entrySet()) {
            double bv = e.getValue();
            nb += bv * bv;
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
