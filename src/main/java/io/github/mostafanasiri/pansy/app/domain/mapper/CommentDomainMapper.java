package io.github.mostafanasiri.pansy.app.domain.mapper;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.domain.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentDomainMapper {
    @Autowired
    private UserDomainMapper userDomainMapper;

    public Comment commentEntityToComment(CommentEntity entity) {
        var user = userDomainMapper.userEntityToUser(entity.getUser());
        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }
}
