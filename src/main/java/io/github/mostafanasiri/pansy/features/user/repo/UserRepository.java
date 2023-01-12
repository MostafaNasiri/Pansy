package io.github.mostafanasiri.pansy.features.user.repo;

import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByUsername(String username); // TODO: Wrap return type in Optional
}
