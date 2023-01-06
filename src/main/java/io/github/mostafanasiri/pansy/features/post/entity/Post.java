package io.github.mostafanasiri.pansy.features.post.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.features.file.File;
import io.github.mostafanasiri.pansy.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Post extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private List<File> images;

    public Post(User creator, String text) {
        this.creator = creator;
        this.text = text;
    }

    public void setImages(List<File> images) {
        this.images = images;
    }
}
