package io.github.mostafanasiri.pansy.features.post.data.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_post_id", columnList = "post_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class CommentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    public CommentEntity(UserEntity user, PostEntity post, String text) {
        this.user = user;
        this.post = post;
        this.text = text;
    }
}
