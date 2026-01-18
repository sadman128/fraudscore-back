package com.sajid.fraudscore.service;

import com.sajid.fraudscore.model.Comment;
import com.sajid.fraudscore.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getCommentsForPost(String postId, String username) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        for (Comment comment : comments) {
            comment.setOwn(comment.getAuthor().equals(username));
        }
        return comments;
    }

    public Comment addComment(String postId, String author, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public ResponseEntity<?> deleteComment(String commentId, String username) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getAuthor().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete others' comments");
        }

        commentRepository.deleteById(commentId);
        return ResponseEntity.ok().build();

    }
}

