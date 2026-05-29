package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cinema.dto.genre.GenreResponse;
import ru.cinema.service.genre.GenreService;

import java.util.List;

/**
 * GenreController — публичный список жанров (сущность Genre из Этапа 3).
 */
@RestController
@RequestMapping("/api/v1/genres")
@Tag(name = "Genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) { this.genreService = genreService; }

    @GetMapping
    public List<GenreResponse> all() { return genreService.all(); }
}
