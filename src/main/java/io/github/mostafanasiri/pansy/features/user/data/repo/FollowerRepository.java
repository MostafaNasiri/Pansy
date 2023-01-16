package io.github.mostafanasiri.pansy.features.user.data.repo;

import io.github.mostafanasiri.pansy.features.user.data.entity.FollowerEntity;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<FollowerEntity, Integer> {
    FollowerEntity findBySourceUserAndTargetUser(UserEntity sourceUser, UserEntity targetUser);

    // TODO: Use join
    List<FollowerEntity> findAllByTargetUser(UserEntity targetUser);

    // TODO: Use join
    List<FollowerEntity> findAllBySourceUser(UserEntity sourceUser);
}
