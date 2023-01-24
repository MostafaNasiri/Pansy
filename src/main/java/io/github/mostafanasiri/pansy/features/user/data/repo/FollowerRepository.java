package io.github.mostafanasiri.pansy.features.user.data.repo;

import io.github.mostafanasiri.pansy.features.user.data.entity.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<FollowerEntity, Integer> {
    FollowerEntity findBySourceUserAndTargetUser(UserEntity sourceUser, UserEntity targetUser);

    @Query("""
            SELECT f, su, sua
            FROM FollowerEntity f
            INNER JOIN f.sourceUser su
            LEFT JOIN su.avatar sua
            WHERE f.targetUser=?1
            """)
        // TODO: Remove avatar join if not needed
    List<FollowerEntity> getFollowers(UserEntity targetUser, Pageable pageable);

    @Query("""
            SELECT f, tu, tua
            FROM FollowerEntity f
            INNER JOIN f.targetUser tu
            LEFT JOIN tu.avatar tua
            WHERE f.sourceUser=?1
            """)
        // TODO: Remove avatar join if not needed
    List<FollowerEntity> getFollowing(UserEntity sourceUser, Pageable pageable);
}
