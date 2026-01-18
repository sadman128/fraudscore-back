package com.sajid.fraudscore.repository;

import com.sajid.fraudscore.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    List<Post> findAllByOrderByPostedAtDesc();

    List<Post> findByPostedBy(String userId);

    List<Post> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
            String name, String email, String phone);

    List<Post> findByCategory(String category);

}
