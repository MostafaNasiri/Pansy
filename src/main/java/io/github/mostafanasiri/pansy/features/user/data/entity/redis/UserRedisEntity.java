package io.github.mostafanasiri.pansy.features.user.data.entity.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@AllArgsConstructor
@RedisHash("user")
public class UserRedisEntity {
    @Id
    private int id;
    @Indexed
    private String username;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private int postCount;
    private int followerCount;
    private int followingCount;
}
