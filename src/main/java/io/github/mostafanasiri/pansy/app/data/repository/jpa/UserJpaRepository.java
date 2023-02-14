package io.github.mostafanasiri.pansy.app.data.repository.jpa;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
