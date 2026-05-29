package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Загрузка файлов админом (постеры контента и т.п.). Файлы сохраняются в
 * {@code src/main/resources/static/uploads/posters/} и отдаются Spring'ом
 * как обычные статические ресурсы по пути {@code /uploads/posters/{file}}.
 */
@RestController
@RequestMapping("/api/v1/admin/uploads")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Uploads")
public class UploadController {

    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Path POSTERS_DIR = Paths.get("src/main/resources/static/uploads/posters");

    @PostMapping("/poster")
    public Map<String, String> uploadPoster(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл пуст");
        }
        String original = file.getOriginalFilename() == null ? "poster" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.')).toLowerCase()
                : ".jpg";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Поддерживаются jpg/jpeg/png/webp/gif");
        }
        Files.createDirectories(POSTERS_DIR);
        String name = UUID.randomUUID() + ext;
        Path target = POSTERS_DIR.resolve(name);
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return Map.of("url", "/uploads/posters/" + name);
    }
}
