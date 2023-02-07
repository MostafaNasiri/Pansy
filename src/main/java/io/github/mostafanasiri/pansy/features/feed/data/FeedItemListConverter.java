package io.github.mostafanasiri.pansy.features.feed.data;

import io.github.mostafanasiri.pansy.features.feed.data.entity.FeedItem;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter
public class FeedItemListConverter implements AttributeConverter<List<FeedItem>, String> {
    private final static String ITEM_SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(List<FeedItem> attribute) {
        var itemsString = attribute.stream().map(FeedItem::toString).toList();

        return String.join(ITEM_SEPARATOR, itemsString);
    }

    @Override
    public List<FeedItem> convertToEntityAttribute(String dbData) {
        var itemsString = Arrays.asList(dbData.split(ITEM_SEPARATOR));

        return itemsString.stream()
                .map(this::createFeedItem)
                .toList();
    }

    private FeedItem createFeedItem(String item) {
        var parts = item.split("-");

        var userId = Integer.parseInt(parts[0]);
        var postId = Integer.parseInt(parts[1]);
        var createdAt = Long.parseLong(parts[2]);

        return new FeedItem(userId, postId, createdAt);
    }
}
