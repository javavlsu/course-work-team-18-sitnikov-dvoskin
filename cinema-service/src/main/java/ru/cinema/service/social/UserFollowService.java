package ru.cinema.service.social;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.User;
import ru.cinema.model.UserFollow;
import ru.cinema.repository.UserFollowRepository;
import ru.cinema.repository.UserRepository;

import java.util.List;

/**
 * UserFollowService — подписки пользователей друг на друга (сущность UserFollow из Этапа 3).
 */
@Service
@Transactional(readOnly = true)
public class UserFollowService {

    private final UserFollowRepository followRepo;
    private final UserRepository userRepo;

    public UserFollowService(UserFollowRepository followRepo, UserRepository userRepo) {
        this.followRepo = followRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void follow(Long followerId, String targetUsername) {
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> NotFoundException.of("User", followerId));
        User target = userRepo.findByUsername(targetUsername)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + targetUsername));
        if (target.getId().equals(followerId)) {
            throw new ConflictException("Нельзя подписаться на себя");
        }
        if (followRepo.existsByFollower_IdAndFollowing_Id(followerId, target.getId())) return;
        UserFollow uf = new UserFollow();
        uf.setFollower(follower);
        uf.setFollowing(target);
        followRepo.save(uf);
    }

    @Transactional
    public void unfollow(Long followerId, String targetUsername) {
        User target = userRepo.findByUsername(targetUsername)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + targetUsername));
        followRepo.deleteByFollowerAndFollowing(followerId, target.getId());
    }

    public boolean isFollowing(Long followerId, String targetUsername) {
        if (followerId == null) return false;
        return userRepo.findByUsername(targetUsername)
                .map(u -> followRepo.existsByFollower_IdAndFollowing_Id(followerId, u.getId()))
                .orElse(false);
    }

    public long followersCount(String username) {
        return userRepo.findByUsername(username)
                .map(u -> followRepo.countByFollowing_Id(u.getId()))
                .orElse(0L);
    }

    public long followingCount(String username) {
        return userRepo.findByUsername(username)
                .map(u -> followRepo.countByFollower_Id(u.getId()))
                .orElse(0L);
    }

    public List<User> listFollowers(String username) {
        return userRepo.findByUsername(username)
                .map(u -> followRepo.findFollowers(u.getId()))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + username));
    }

    public List<User> listFollowing(String username) {
        return userRepo.findByUsername(username)
                .map(u -> followRepo.findFollowing(u.getId()))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + username));
    }
}
