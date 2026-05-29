package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.PlaylistRating;

import java.util.Optional;

@Repository
public interface PlaylistRatingRepository extends JpaRepository<PlaylistRating, Long> {

    Optional<PlaylistRating> findByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

    long countByPlaylist_Id(Long playlistId);

    @Query("SELECT AVG(pr.value) FROM PlaylistRating pr WHERE pr.playlist.id = :pid")
    Double averageByPlaylist(@Param("pid") Long playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistRating pr WHERE pr.user.id = :userId AND pr.playlist.id = :playlistId")
    int deleteByUserAndPlaylist(@Param("userId") Long userId, @Param("playlistId") Long playlistId);
}
