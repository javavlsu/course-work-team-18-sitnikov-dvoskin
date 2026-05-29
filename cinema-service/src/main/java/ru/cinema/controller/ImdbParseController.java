package ru.cinema.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Поиск фильма/сериала через публичный IMDB autocomplete (sg.media-imdb.com).
 * Без ключа. Возвращает title, year, type, imdbId, posterUrl — то что нужно
 * для предзаполнения формы создания контента в админке.
 */
@RestController
@RequestMapping("/api/v1/admin/parse")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Parser")
public class ImdbParseController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    /**
     * Перевод текста через MyMemory API (free, 5000 chars/day anonymous, no key).
     */
    @GetMapping("/translate")
    public ResponseEntity<?> translate(@RequestParam("q") String text,
                                       @RequestParam(value = "from", defaultValue = "en") String from,
                                       @RequestParam(value = "to",   defaultValue = "ru") String to) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пустой текст"));
        }
        // MyMemory имеет лимит на длину одного запроса 500 chars; разбиваем
        String trimmed = text.length() > 500 ? text.substring(0, 500) : text;
        String pair = from + "|" + to;
        String url = "https://api.mymemory.translated.net/get?q="
                + URLEncoder.encode(trimmed, StandardCharsets.UTF_8)
                + "&langpair=" + URLEncoder.encode(pair, StandardCharsets.UTF_8);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "MovieHub-Coursework/1.0")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "MyMemory вернул " + resp.statusCode()));
            }
            JsonNode root = mapper.readTree(resp.body());
            String translated = root.path("responseData").path("translatedText").asText("");
            if (translated.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Перевод не получен"));
            }
            return ResponseEntity.ok(Map.of("text", translated, "from", from, "to", to));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Ошибка перевода", "message", e.getMessage()));
        }
    }

    /**
     * Wikidata SPARQL по IMDB ID — структурированные поля: страна, язык,
     * длительность, бюджет, сборы, сезоны, эпизоды. Без ключа.
     */
    @GetMapping("/wikidata")
    public ResponseEntity<?> wikidata(@RequestParam("imdbId") String imdbId) {
        if (!imdbId.matches("tt\\d{5,12}")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Невалидный IMDB ID"));
        }
        String sparql =
                "SELECT ?countryLabel ?langLabel ?duration ?budget ?boxoffice ?seasons ?episodes WHERE { " +
                "  ?film wdt:P345 \"" + imdbId + "\". " +
                "  OPTIONAL { ?film wdt:P495 ?country. } " +
                "  OPTIONAL { ?film wdt:P364 ?lang. } " +
                "  OPTIONAL { ?film wdt:P2047 ?duration. } " +
                "  OPTIONAL { ?film wdt:P2130 ?budget. } " +
                "  OPTIONAL { ?film wdt:P2142 ?boxoffice. } " +
                "  OPTIONAL { ?film wdt:P2437 ?seasons. } " +
                "  OPTIONAL { ?film wdt:P1113 ?episodes. } " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"ru,en\". } " +
                "} ORDER BY ?langLabel LIMIT 50";
        String url = "https://query.wikidata.org/sparql?format=json&query="
                + URLEncoder.encode(sparql, StandardCharsets.UTF_8);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "MovieHub-Coursework/1.0 (educational)")
                    .header("Accept", "application/sparql-results+json")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Wikidata вернула " + resp.statusCode()));
            }
            JsonNode root = mapper.readTree(resp.body());
            java.util.LinkedHashSet<String> countries = new java.util.LinkedHashSet<>();
            java.util.LinkedHashSet<String> langs     = new java.util.LinkedHashSet<>();
            Integer duration = null, seasons = null, episodes = null;
            java.math.BigDecimal budget = null;
            java.math.BigDecimal box = java.math.BigDecimal.ZERO; boolean hasBox = false;
            for (JsonNode b : root.path("results").path("bindings")) {
                addText(b, "countryLabel", countries);
                addText(b, "langLabel", langs);
                if (duration == null) duration = parseInt(b.path("duration"));
                if (seasons  == null) seasons  = parseInt(b.path("seasons"));
                if (episodes == null) episodes = parseInt(b.path("episodes"));
                if (budget   == null) budget   = parseDecimal(b.path("budget"));
                java.math.BigDecimal bx = parseDecimal(b.path("boxoffice"));
                if (bx != null) { if (bx.compareTo(box) > 0) { box = bx; hasBox = true; } }
            }
            Map<String, Object> out = new HashMap<>();
            if (!countries.isEmpty()) out.put("country", trimLang(countries.iterator().next()));
            if (!langs.isEmpty())     out.put("language", capitalize(stripLanguageSuffix(langs.iterator().next())));
            if (duration != null) out.put("duration", duration);
            if (seasons  != null) out.put("totalSeasons", seasons);
            if (episodes != null) out.put("totalEpisodes", episodes);
            if (budget != null) out.put("budget", budget);
            if (hasBox) out.put("boxOffice", box);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Ошибка обращения к Wikidata", "message", e.getMessage()));
        }
    }

    private static void addText(JsonNode binding, String key, java.util.Collection<String> out) {
        JsonNode n = binding.path(key);
        if (n.isMissingNode() || n.isNull()) return;
        String v = n.path("value").asText("");
        if (!v.isBlank() && !v.startsWith("http")) out.add(v);
    }

    private static Integer parseInt(JsonNode n) {
        if (n.isMissingNode() || n.isNull()) return null;
        String v = n.path("value").asText("");
        try { return v.isBlank() ? null : (int) Math.round(Double.parseDouble(v)); }
        catch (NumberFormatException e) { return null; }
    }

    private static java.math.BigDecimal parseDecimal(JsonNode n) {
        if (n.isMissingNode() || n.isNull()) return null;
        String v = n.path("value").asText("");
        try { return v.isBlank() ? null : new java.math.BigDecimal(v); }
        catch (NumberFormatException e) { return null; }
    }

    private static String trimLang(String s) {
        if (s == null) return null;
        return s.replaceAll("\\s+$", "");
    }

    private static String stripLanguageSuffix(String s) {
        if (s == null) return null;
        String t = s.trim();
        // «Американский вариант английского языка» → «Английский»
        var m = java.util.regex.Pattern
                .compile("вариант\\s+(\\p{L}+)\\s+языка", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(t);
        if (m.find()) {
            String base = m.group(1).toLowerCase();
            if      (base.endsWith("ого")) base = base.substring(0, base.length() - 3) + "ий";
            else if (base.endsWith("его")) base = base.substring(0, base.length() - 3) + "ий";
            return capitalize(base);
        }
        if (t.endsWith(" язык")) t = t.substring(0, t.length() - 5);
        else if (t.endsWith(" language")) t = t.substring(0, t.length() - 9);
        return t;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пустой запрос"));
        }
        String query = q.trim().toLowerCase().replaceAll("\\s+", "_");
        // IMDB suggest шарданится по первой букве; если первая буква нелатинская — берём первый ascii char
        char letter = 'a';
        for (char c : query.toCharArray()) {
            if (c >= 'a' && c <= 'z') { letter = c; break; }
            if (c >= '0' && c <= '9') { letter = c; break; }
        }
        String urlEncoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://v3.sg.media-imdb.com/suggestion/" + letter + "/" + urlEncoded + ".json";

        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "MovieHub-Admin/1.0")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "IMDB вернул " + resp.statusCode()));
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode arr = root.path("d");
            List<Map<String, Object>> results = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    String id = n.path("id").asText("");
                    if (!id.startsWith("tt")) continue; // отсеиваем имена людей (nm...) и прочее
                    String qualifier = n.path("q").asText("").toLowerCase();
                    String type;
                    if (qualifier.contains("series")) type = "SERIES";
                    else if (qualifier.contains("feature") || qualifier.contains("video") || qualifier.contains("short")) type = "MOVIE";
                    else type = null;
                    Map<String, Object> item = new HashMap<>();
                    item.put("imdbId", id);
                    item.put("title", n.path("l").asText(null));
                    item.put("originalTitle", n.path("l").asText(null));
                    item.put("releaseYear", n.path("y").isInt() ? n.path("y").asInt() : null);
                    item.put("type", type);
                    item.put("posterUrl", n.path("i").path("imageUrl").asText(null));
                    item.put("stars", listOf(n.path("s")));
                    results.add(item);
                }
            }
            return ResponseEntity.ok(Map.of("query", q, "results", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Ошибка обращения к IMDB", "message", e.getMessage()));
        }
    }

    private static String listOf(JsonNode n) {
        if (n == null || n.isNull()) return null;
        return n.asText(null);
    }
}
