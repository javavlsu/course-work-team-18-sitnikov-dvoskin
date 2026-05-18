package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.User;
import ru.cinema.model.UserFollow;

import java.util.List;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollow.PK> {

    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    @Modifying
    @Query("DELETE FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id = :followingId")
    int deleteByFollowerAndFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    long countByFollowing_Id(Long userId);
    long countByFollower_Id(Long userId);

    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.following.id = :userId ORDER BY uf.createdAt DESC")
    List<User> findFollowers(@Param("userId") Long userId);

    @Query("SELECT uf.following FROM UserFollow uf WHERE uf.follower.id = :userId ORDER BY uf.createdAt DESC")
    List<User> findFollowing(@Param("userId") Long userId);
}
