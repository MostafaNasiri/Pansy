package io.github.mostafanasiri.pansy.features.user.data.repo.jpa;

import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
