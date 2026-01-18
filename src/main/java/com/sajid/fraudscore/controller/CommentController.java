package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.component.JwtService;
import com.sajid.fraudscore.dto.CommentDto;
import com.sajid.fraudscore.model.Comment;
import com.sajid.fraudscore.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
//@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;
    private final JwtService jwtService;

    public CommentController(CommentService commentService, JwtService jwtService) {
        this.commentService = commentService;
        this.jwtService = jwtService;
    }


    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable String postId, @RequestHeader("Authorization") String auth) {
        String username = jwtService.extractSubject(auth.substring(7));
        return commentService.getCommentsForPost(postId,  username);
    }

    @PostMapping("/{postId}/comments")
    public Comment addComment(
            @PathVariable String postId,
            @RequestBody String content,
            @RequestHeader("Authorization") String auth  // From your JWT filter
    ) {
        String username = jwtService.extractSubject(auth.substring(7));
        return commentService.addComment(postId, username, content);
    }



    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String commentId,
            Authentication authentication,
            @PathVariable String postId) {
        String username = authentication.getName();
        IO.println("deleteComment: " + commentId);

        return commentService.deleteComment(commentId,username);

    }




}
