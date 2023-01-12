package io.github.mostafanasiri.pansy.features.user.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "followers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_user_id", "target_user_id"})
        // TODO: Add index
)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
public class Follower extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "source_user_id", nullable = false)
    private User sourceUser;

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;
}
