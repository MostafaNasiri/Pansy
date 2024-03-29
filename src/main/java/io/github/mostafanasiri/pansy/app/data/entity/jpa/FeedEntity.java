package io.github.mostafanasiri.pansy.app.data.entity.jpa;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "feeds")
@EntityListeners(AuditingEntityListener.class)
public class FeedEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private UserEntity user;

    @Type(JsonType.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Deque<FeedItem> items = new ArrayDeque<>();

    public FeedEntity(UserEntity user) {
        this.user = user;
    }

    public void setItems(Deque<FeedItem> items) {
        this.items = items;
    }

    public record FeedItem(int userId, int postId) {
    }
}
