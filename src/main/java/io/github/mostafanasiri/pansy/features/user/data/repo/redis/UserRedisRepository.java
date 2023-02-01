package io.github.mostafanasiri.pansy.features.user.data.repo.redis;

import io.github.mostafanasiri.pansy.features.user.data.entity.redis.UserRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRedisRepository extends CrudRepository<UserRedis, Integer> {
}
