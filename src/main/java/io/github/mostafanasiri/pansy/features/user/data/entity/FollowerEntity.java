package io.github.mostafanasiri.pansy.features.user.data.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "followers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_user_id", "target_user_id"}),
        indexes = {
                @Index(columnList = "source_user_id"),
                @Index(columnList = "target_user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class FollowerEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "source_user_id", nullable = false)
    private UserEntity sourceUser;

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private UserEntity targetUser;
}
