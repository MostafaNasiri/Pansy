package io.github.mostafanasiri.pansy.app.data.entity.jpa.notification;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(columnList = "notified_user_id")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
public abstract class NotificationEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notifier_user_id", nullable = false)
    private UserEntity notifierUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notified_user_id", nullable = false)
    private UserEntity notifiedUser;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public NotificationEntity(UserEntity notifierUser, UserEntity notifiedUser) {
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }
}
