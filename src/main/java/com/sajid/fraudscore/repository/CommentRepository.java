package com.sajid.fraudscore.repository;

import com.sajid.fraudscore.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);

    List<Comment> findByPostIdIn(List<String> postIds);
}
