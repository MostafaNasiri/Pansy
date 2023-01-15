package io.github.mostafanasiri.pansy.features.user.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.file.File;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(columnList = "username")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity extends BaseEntity {
    @Column(name = "first_name", nullable = false)
    private String fullName;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "bio")
    private String bio;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "avatar_id")
    private File avatar;

    @Column(name = "follower_count", nullable = false)
    private int followerCount = 0;

    @Column(name = "following_count", nullable = false)
    private int followingCount = 0;

    public UserEntity(@NonNull String fullName, @NonNull String username, @NonNull String password) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setAvatar(File avatar) {
        this.avatar = avatar;
    }

    public void incrementFollowerCount() {
        followerCount++;
    }

    public void decrementFollowerCount() {
        if (followerCount > 0) {
            followerCount--;
        }
    }

    public void incrementFollowingCount() {
        followingCount++;
    }

    public void decrementFollowingCount() {
        if (followingCount > 0) {
            followingCount--;
        }
    }
}
