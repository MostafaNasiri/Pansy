package io.github.mostafanasiri.pansy.features.user.data.repo.redis;

import io.github.mostafanasiri.pansy.features.user.data.entity.redis.RedisUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisUserRepository extends CrudRepository<RedisUser, Integer> {
}
