package io.github.mostafanasiri.pansy.features.user.entity;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "followers")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Follower extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "source_user_id")
    private User sourceUser;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser;
}
