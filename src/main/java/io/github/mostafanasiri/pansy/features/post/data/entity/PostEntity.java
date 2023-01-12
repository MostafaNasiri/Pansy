package io.github.mostafanasiri.pansy.features.post.data.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.file.File;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(columnList = "user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class PostEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "caption", nullable = false, length = 1000)
    private String caption;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "posts_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private List<File> images;

    public PostEntity(@NonNull UserEntity user, @NonNull String caption, @NonNull List<File> images) {
        this.user = user;
        this.caption = caption;
        this.images = images;
    }

    public void setImages(List<File> images) {
        this.images = images;
    }
}
