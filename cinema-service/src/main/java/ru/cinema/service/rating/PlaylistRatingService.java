package ru.cinema.service.rating;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.rating.PlaylistRatingResponse;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Playlist;
import ru.cinema.model.PlaylistRating;
import ru.cinema.repository.PlaylistRatingRepository;
import ru.cinema.repository.PlaylistRepository;

/**
 * PlaylistRatingService — оценки подборок (сущность CollectionRating из Этапа 3).
 * Шкала 1–5.
 */
@Service
@Transactional(readOnly = true)
public class PlaylistRatingService {

    private final PlaylistRatingRepository prRepo;
    private final PlaylistRepository playlistRepo;
    private final ru.cinema.service.user.UserService userService;

    public PlaylistRatingService(PlaylistRatingRepository prRepo,
                                  PlaylistRepository playlistRepo,
                                  ru.cinema.service.user.UserService userService) {
        this.prRepo = prRepo;
        this.playlistRepo = playlistRepo;
        this.userService = userService;
    }

    @Transactional
    public PlaylistRatingResponse rate(Long userId, Long playlistId, Integer value) {
        if (value == null || value < 1 || value > 5) {
            throw new IllegalArgumentException("Оценка от 1 до 5");
        }
        Playlist p = playlistRepo.findById(playlistId)
                .orElseThrow(() -> NotFoundException.of("Playlist", playlistId));
        PlaylistRating pr = prRepo.findByUser_IdAndPlaylist_Id(userId, playlistId).orElseGet(() -> {
            PlaylistRating x = new PlaylistRating();
            x.setUser(userService.getById(userId));
            x.setPlaylist(p);
            return x;
        });
        pr.setValue(value);
        pr = prRepo.save(pr);
        long count = prRepo.countByPlaylist_Id(playlistId);
        Double avg = prRepo.averageByPlaylist(playlistId);
        return PlaylistRatingResponse.of(pr, avg == null ? 0.0 : avg, count);
    }

    @Transactional
    public void remove(Long userId, Long playlistId) {
        prRepo.deleteByUserAndPlaylist(userId, playlistId);
    }

    public double averageFor(Long playlistId) {
        Double avg = prRepo.averageByPlaylist(playlistId);
        return avg == null ? 0.0 : avg;
    }

    public long countFor(Long playlistId) { return prRepo.countByPlaylist_Id(playlistId); }
}
