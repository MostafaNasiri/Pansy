package io.github.mostafanasiri.pansy.features.user.repo;

import io.github.mostafanasiri.pansy.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username); // TODO: Wrap return type in Optional
}
