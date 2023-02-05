package io.github.mostafanasiri.pansy.features.user.data.repo.jpa;

import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerJpaRepository extends JpaRepository<FollowerEntity, Integer> {
    FollowerEntity findBySourceUserAndTargetUser(UserEntity sourceUser, UserEntity targetUser);

    @Query("""
            SELECT f
            FROM FollowerEntity f
            INNER JOIN FETCH f.sourceUser su
            LEFT JOIN FETCH su.avatar
            WHERE f.targetUser=?1
            """)
    List<FollowerEntity> getFollowers(UserEntity targetUser, Pageable pageable);

    @Query("""
            SELECT f
            FROM FollowerEntity f
            INNER JOIN FETCH f.targetUser tu
            LEFT JOIN FETCH tu.avatar tua
            WHERE f.sourceUser=?1
            """)
    List<FollowerEntity> getFollowing(UserEntity sourceUser, Pageable pageable);

    @Query("""
            SELECT COUNT(f.id)
            FROM FollowerEntity f
            WHERE f.targetUser=?1
            """)
    int getFollowerCount(UserEntity userEntity);

    @Query("""
            SELECT COUNT(f.id)
            FROM FollowerEntity f
            WHERE f.sourceUser=?1
            """)
    int getFollowingCount(UserEntity userEntity);
}
