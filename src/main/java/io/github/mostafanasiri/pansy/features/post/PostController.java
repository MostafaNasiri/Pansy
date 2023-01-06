package io.github.mostafanasiri.pansy.features.post;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post")
@RestController
public class PostController {
    // TODO - [GET] /users/{user_id}/posts - Returns a user's posts

    // TODO - [POST] /posts - Creates a new post

    // TODO - [PUT] /posts/{post_id} - Edits a post

    // TODO - [DELETE] /posts/{post_id} - Deletes a post


    // TODO - [POST] /posts/{post_id}/likes - Likes the specified post by the authorized user

    // TODO - [GET] /posts/{post_id}/likes - Returns a list of users who liked the specified post id

    // TODO - [DELETE] /posts/{post_id}/likes/{user_id} - Unlikes a post that has already been liked by the authorized user


    // TODO - [GET] /posts/{post_id}/comments - Returns comments of the specified post id

    // TODO - [POST] /posts/{post_id}/comments - Adds a comment for the specified post id

    // TODO - [DELETE] /posts/{post_id}/comments/{comment_id} - Removes a comment from the specified post id
}
