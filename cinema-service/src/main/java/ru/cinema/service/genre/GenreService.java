package ru.cinema.service.genre;

import org.springframework.stereotype.Service;
import ru.cinema.dto.genre.GenreResponse;
import ru.cinema.repository.GenreRepository;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepo;

    public GenreService(GenreRepository genreRepo) { this.genreRepo = genreRepo; }

    public List<GenreResponse> all() {
        return genreRepo.findAllByOrderByNameAsc().stream().map(GenreResponse::of).toList();
    }
}
