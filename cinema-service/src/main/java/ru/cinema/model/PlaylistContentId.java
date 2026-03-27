package ru.cinema.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Составной ключ для сущности {@link PlaylistContent}.
 */
public class PlaylistContentId implements Serializable {

    private Long playlist;
    private Long content;

    public PlaylistContentId() {
    }

    public PlaylistContentId(Long playlist, Long content) {
        this.playlist = playlist;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistContentId that = (PlaylistContentId) o;
        return Objects.equals(playlist, that.playlist) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlist, content);
    }
}
