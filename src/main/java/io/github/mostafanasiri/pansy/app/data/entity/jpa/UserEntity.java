package io.github.mostafanasiri.pansy.app.data.entity.jpa;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Indexed(index = "UserIndex")
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(columnList = "username")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity extends BaseEntity {
    @FullTextField
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @FullTextField
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "bio")
    private String bio;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "avatar_images",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private FileEntity avatar;

    @Column(name = "post_count", nullable = false)
    private int postCount = 0;

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

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setAvatar(FileEntity avatar) {
        this.avatar = avatar;
    }

    public void setPostCount(int postCount) {
        if (postCount >= 0) {
            this.postCount = postCount;
        }
    }

    public void setFollowerCount(int followerCount) {
        if (followerCount >= 0) {
            this.followerCount = followerCount;
        }
    }

    public void setFollowingCount(int followingCount) {
        if (followingCount >= 0) {
            this.followingCount = followingCount;
        }
    }
}
