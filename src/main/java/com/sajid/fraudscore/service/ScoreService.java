package com.sajid.fraudscore.service;

import com.sajid.fraudscore.dto.FraudAnalysisDto;
import com.sajid.fraudscore.dto.FraudAnalysisResult;
import com.sajid.fraudscore.model.Comment;
import com.sajid.fraudscore.model.Post;
import com.sajid.fraudscore.repository.CommentRepository;
import com.sajid.fraudscore.repository.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScoreService {
    public final AiService aiService;
    public final PostRepository postRepository;
    public final CommentRepository commentRepository;
    private final MongoTemplate mongoTemplate;


    public ScoreService(AiService aiService, PostRepository postRepository, CommentRepository commentRepository, MongoTemplate mongoTemplate) {
        this.aiService = aiService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> getScore(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return null;
        }

        String cleanPrompt = prompt.trim();
        IO.println("Searching for: " + cleanPrompt);

        // Build Mongo query similar to working searchPosts
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("name").regex(cleanPrompt, "i"),
                Criteria.where("email").regex(cleanPrompt, "i"),
                Criteria.where("phone").regex(cleanPrompt, "i"),
                Criteria.where("address").regex(cleanPrompt, "i"),
                Criteria.where("title").regex(cleanPrompt, "i"),
                Criteria.where("description").regex(cleanPrompt, "i"),
                Criteria.where("category").regex(cleanPrompt, "i")
        );

        Query query = new Query(criteria).limit(50); // limit for performance

        List<Post> posts = mongoTemplate.find(query, Post.class);

        if (posts.isEmpty()) {
            return null;
        }

        List<String> postIds = posts.stream().map(Post::getId).toList();
        List<Comment> comments = commentRepository.findByPostIdIn(postIds);

        FraudAnalysisDto dto = new FraudAnalysisDto();
        dto.setEntityName(prompt);
        dto.setPosts(posts);
        dto.setComments(comments);

        // Send to AI service
        return aiService.analyzeFraud(dto);
    }



}
