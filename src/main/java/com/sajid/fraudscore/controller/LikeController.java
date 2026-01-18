package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.model.Post;
import com.sajid.fraudscore.service.LikeService;
import com.sajid.fraudscore.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class LikeController {
    public final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }


    @PostMapping("/{postId}/like")
    public ResponseEntity<Post> likePost(
            @PathVariable String postId,
            Authentication authentication) {
        String currentUsername = authentication.getName(); // from JWT
        Post updatedPost = likeService.likePost(postId, currentUsername);
        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/{postId}/dislike")
    public ResponseEntity<Post> dislikePost(
            @PathVariable String postId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        Post updatedPost = likeService.dislikePost(postId, currentUsername);
        return ResponseEntity.ok(updatedPost);
    }



}

