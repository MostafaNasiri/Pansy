package io.github.mostafanasiri.pansy.features.post.data.repository.redis;

import io.github.mostafanasiri.pansy.features.post.data.entity.redis.PostRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRedisRepository extends CrudRepository<PostRedis, Integer> {

}
