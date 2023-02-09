package io.github.mostafanasiri.pansy.features.feed.data.entity;

import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "feeds")
public class FeedEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @OneToOne(mappedBy = "feed")
    private UserEntity user;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<FeedItem> items;
}
