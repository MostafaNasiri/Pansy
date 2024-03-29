package io.github.mostafanasiri.pansy.app.data.entity.jpa;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "likes",
        indexes = {
                @Index(columnList = "user_id"),
                @Index(columnList = "post_id")
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@EntityListeners(AuditingEntityListener.class)
public class LikeEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    public LikeEntity(@NonNull UserEntity user, @NonNull PostEntity post) {
        this.user = user;
        this.post = post;
    }
}
