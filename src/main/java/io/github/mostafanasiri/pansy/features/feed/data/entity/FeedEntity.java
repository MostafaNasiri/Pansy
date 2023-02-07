package io.github.mostafanasiri.pansy.features.feed.data.entity;

import io.github.mostafanasiri.pansy.features.feed.data.FeedItemListConverter;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "feeds",
        indexes = {
                @Index(columnList = "user_id")
        }
)
public class FeedEntity {
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "items")
    @Convert(converter = FeedItemListConverter.class)
    private List<FeedItem> items;

    public FeedEntity(UserEntity user) {
        this.user = user;
    }
}
