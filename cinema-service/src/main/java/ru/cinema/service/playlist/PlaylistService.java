package ru.cinema.service.playlist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.playlist.AddPlaylistItemRequest;
import ru.cinema.dto.playlist.CreatePlaylistRequest;
import ru.cinema.dto.playlist.ReorderItemsRequest;
import ru.cinema.dto.playlist.UpdatePlaylistRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Playlist;
import ru.cinema.model.PlaylistContent;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.PlaylistRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepo;
    private final ContentRepository contentRepo;
    private final ru.cinema.service.user.UserService userService;

    public PlaylistService(PlaylistRepository playlistRepo,
                           ContentRepository contentRepo,
                           ru.cinema.service.user.UserService userService) {
        this.playlistRepo = playlistRepo;
        this.contentRepo = contentRepo;
        this.userService = userService;
    }

    public Playlist getById(Long id) {
        return playlistRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("Playlist", id));
    }

    public Page<Playlist> list(Long userId, Boolean isPublic, Pageable pageable) {
        if (userId != null) {
            // делаем синтетическую страницу
            var list = playlistRepo.findByUserIdOrderByCreatedAtDesc(userId);
            int from = (int) pageable.getOffset();
            int to = Math.min(from + pageable.getPageSize(), list.size());
            var slice = from >= list.size() ? java.util.List.<Playlist>of() : list.subList(from, to);
            return new org.springframework.data.domain.PageImpl<>(slice, pageable, list.size());
        }
        return playlistRepo.findByIsPublic(isPublic == null ? Boolean.TRUE : isPublic, pageable);
    }

    public Playlist getVisible(Long id, Long currentUserId) {
        Playlist p = getById(id);
        if (Boolean.FALSE.equals(p.getIsPublic())
                && (currentUserId == null || !p.getUser().getId().equals(currentUserId))) {
            throw new ForbiddenException("Подборка приватная");
        }
        return p;
    }

    @Transactional
    public Playlist create(Long currentUserId, CreatePlaylistRequest req) {
        Playlist p = new Playlist();
        p.setUser(userService.getById(currentUserId));
        p.setTitle(req.title());
        p.setDescription(req.description());
        p.setCoverImageUrl(req.coverImageUrl());
        p.setIsPublic(req.isPublic() == null ? Boolean.TRUE : req.isPublic());
        return playlistRepo.save(p);
    }

    @Transactional
    public Playlist update(Long id, Long currentUserId, UpdatePlaylistRequest req) {
        Playlist p = getById(id);
        ensureOwner(p, currentUserId);
        if (req.title() != null) p.setTitle(req.title());
        if (req.description() != null) p.setDescription(req.description());
        if (req.coverImageUrl() != null) p.setCoverImageUrl(req.coverImageUrl());
        if (req.isPublic() != null) p.setIsPublic(req.isPublic());
        return playlistRepo.save(p);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        Playlist p = getById(id);
        ensureOwner(p, currentUserId);
        playlistRepo.delete(p);
    }

    @Transactional
    public Playlist addItem(Long id, Long currentUserId, AddPlaylistItemRequest req) {
        Playlist p = getById(id);
        ensureOwner(p, currentUserId);
        if (playlistRepo.containsContent(p.getId(), req.contentId())) {
            throw new ConflictException("Контент уже в подборке");
        }
        Content content = contentRepo.findById(req.contentId())
                .orElseThrow(() -> NotFoundException.of("Content", req.contentId()));
        PlaylistContent pc = new PlaylistContent();
        pc.setPlaylist(p);
        pc.setContent(content);
        pc.setAddedAt(LocalDateTime.now());
        int order = req.sortOrder() != null ? req.sortOrder() : (p.getPlaylistContents().size() + 1);
        pc.setSortOrder(order);
        p.getPlaylistContents().add(pc);
        return playlistRepo.save(p);
    }

    @Transactional
    public Playlist removeItem(Long id, Long currentUserId, Long contentId) {
        Playlist p = getById(id);
        ensureOwner(p, currentUserId);
        boolean removed = p.getPlaylistContents().removeIf(pc ->
                pc.getContent() != null && contentId.equals(pc.getContent().getId()));
        if (!removed) throw new NotFoundException("Контент в подборке не найден: id=" + contentId);
        return playlistRepo.save(p);
    }

    @Transactional
    public Playlist reorder(Long id, Long currentUserId, ReorderItemsRequest req) {
        Playlist p = getById(id);
        ensureOwner(p, currentUserId);
        Map<Long, Integer> orderMap = new HashMap<>();
        for (var item : req.items()) {
            orderMap.put(item.contentId(), item.sortOrder());
        }
        for (PlaylistContent pc : p.getPlaylistContents()) {
            Integer target = orderMap.get(pc.getContent().getId());
            if (target != null) pc.setSortOrder(target);
        }
        return playlistRepo.save(p);
    }

    private void ensureOwner(Playlist p, Long userId) {
        if (userId == null || !p.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Действие доступно только владельцу подборки");
        }
    }
}
