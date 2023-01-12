package io.github.mostafanasiri.pansy.features.user.repo;

import io.github.mostafanasiri.pansy.features.user.entity.Follower;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Integer> {
    Follower findBySourceUserAndTargetUser(UserEntity sourceUser, UserEntity targetUser);

    List<Follower> findAllByTargetUser(UserEntity targetUser);

    List<Follower> findAllBySourceUser(UserEntity sourceUser);
}
