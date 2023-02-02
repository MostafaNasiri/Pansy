package io.github.mostafanasiri.pansy.features.post.data.entity.redis;

import io.github.mostafanasiri.pansy.features.user.data.entity.redis.UserRedis;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash("post")
public record PostRedis(
        @Id int id,
        @Reference UserRedis user,
        String caption,
        List<String> imageNames,
        int likeCount,
        int commentCount
) {

}
