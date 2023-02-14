package io.github.mostafanasiri.pansy.app.data.entity.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import java.util.Date;
import java.util.List;

@RedisHash("post")
public record PostRedis(
        @Id int id,
        @Reference UserRedis user,
        String caption,
        List<String> imageNames,
        int likeCount,
        int commentCount,
        Date createdAt
) {

}
