package io.github.mostafanasiri.pansy.app.data.entity.jpa.notification;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("comment")
public class CommentNotificationEntity extends NotificationEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommentEntity comment;

    public CommentNotificationEntity(
            @NonNull UserEntity notifierUser,
            @NonNull UserEntity notifiedUser,
            @NonNull PostEntity post,
            @NonNull CommentEntity comment
    ) {
        super(notifierUser, notifiedUser);
        this.post = post;
        this.comment = comment;
    }
}
