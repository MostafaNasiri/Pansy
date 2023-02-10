package io.github.mostafanasiri.pansy.features.user.data.repo.jpa;

import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.FollowerEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowerJpaRepository extends JpaRepository<FollowerEntity, Integer> {
    @Query("""
            SELECT f
            FROM FollowerEntity f
            WHERE f.sourceUser.id=?1 AND f.targetUser.id=?2
            """)
    Optional<FollowerEntity> findBySourceUserAndTargetUser(int sourceUserId, int targetUserId);

    @Query("""
            SELECT f
            FROM FollowerEntity f
            INNER JOIN FETCH f.sourceUser su
            LEFT JOIN FETCH su.avatar
            WHERE f.targetUser.id=?1
            """)
    List<FollowerEntity> getFollowers(int targetUserId, Pageable pageable);

    @Query("""
            SELECT f
            FROM FollowerEntity f
            INNER JOIN FETCH f.targetUser tu
            LEFT JOIN FETCH tu.avatar tua
            WHERE f.sourceUser.id=?1
            """)
    List<FollowerEntity> getFollowing(int sourceUserId, Pageable pageable);

    @Query("""
            SELECT COUNT(f.id)
            FROM FollowerEntity f
            WHERE f.targetUser.id=?1
            """)
    int getFollowerCount(int userId);

    @Query("""
            SELECT COUNT(f.id)
            FROM FollowerEntity f
            WHERE f.sourceUser.id=?1
            """)
    int getFollowingCount(int userId);

    @Query("""
            SELECT f.sourceUser.id
            FROM FollowerEntity f
            WHERE f.targetUser.id=?1
            """)
    List<Integer> getFollowersIds(int userId);
}
