package io.github.mostafanasiri.pansy.features.user.data.repo;

import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    @Query(
            value = "SELECT user_id FROM avatar_images WHERE file_id=?1",
            nativeQuery = true
    )
    Optional<Integer> getUserIdByAvatarFileId(int fileId);

    Optional<UserEntity> findByUsername(String username);
}
