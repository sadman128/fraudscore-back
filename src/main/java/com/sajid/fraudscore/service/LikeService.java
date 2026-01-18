package com.sajid.fraudscore.service;

import com.sajid.fraudscore.model.Post;
import com.sajid.fraudscore.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class LikeService {
    public final PostRepository postRepository;

    public LikeService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post likePost(String postId, String currentUsername) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setLikedPostUsers(post.getLikedPostUsers() != null ?
                post.getLikedPostUsers() : new ArrayList<>());

        post.setDislikedPostUsers(post.getDislikedPostUsers() != null ?
                post.getDislikedPostUsers() : new ArrayList<>());

        // Toggle logic
        if (post.getLikedPostUsers().contains(currentUsername)) {
            // Already liked -> remove like
            post.getLikedPostUsers().remove(currentUsername);
        } else {
            // Add like, remove dislike if exists
            post.getLikedPostUsers().add(currentUsername);
            post.getDislikedPostUsers().remove(currentUsername);
        }

        // Update counts and flags
        post.setLikedCount(post.getLikedPostUsers().size());
        post.setDislikedCount(post.getDislikedPostUsers().size());
        post.setOwnLiked(post.getLikedPostUsers().contains(currentUsername));
        post.setOwnDisliked(post.getDislikedPostUsers().contains(currentUsername));

        return postRepository.save(post);
    }

    public Post dislikePost(String postId, String currentUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setLikedPostUsers(post.getLikedPostUsers() != null ?
                post.getLikedPostUsers() : new ArrayList<>());
        post.setDislikedPostUsers(post.getDislikedPostUsers() != null ?
                post.getDislikedPostUsers() : new ArrayList<>());

        if (post.getDislikedPostUsers().contains(currentUsername)) {
            // Already disliked -> remove dislike
            post.getDislikedPostUsers().remove(currentUsername);
        } else {
            // Add dislike, remove like if exists
            post.getDislikedPostUsers().add(currentUsername);
            post.getLikedPostUsers().remove(currentUsername);
        }

        post.setLikedCount(post.getLikedPostUsers().size());
        post.setDislikedCount(post.getDislikedPostUsers().size());
        post.setOwnLiked(post.getLikedPostUsers().contains(currentUsername));
        post.setOwnDisliked(post.getDislikedPostUsers().contains(currentUsername));

        return postRepository.save(post);
    }


}
