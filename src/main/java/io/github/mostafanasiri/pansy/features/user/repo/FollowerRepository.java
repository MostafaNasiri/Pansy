package io.github.mostafanasiri.pansy.features.user.repo;

import io.github.mostafanasiri.pansy.features.user.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Integer> {
}
