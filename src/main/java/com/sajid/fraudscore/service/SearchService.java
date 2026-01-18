package com.sajid.fraudscore.service;

import com.sajid.fraudscore.dto.PostDto;
import com.sajid.fraudscore.dto.PostSearchResponse;
import com.sajid.fraudscore.model.Post;
import com.sajid.fraudscore.repository.PostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SearchService {

    private final MongoTemplate mongoTemplate;

    public SearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PostSearchResponse searchPosts(String q) {
        if (q == null || q.trim().isEmpty()) {
            return new PostSearchResponse(List.of(), List.of());
        }

        Criteria c = new Criteria().orOperator(
                Criteria.where("name").regex(q, "i"),
                Criteria.where("email").regex(q, "i"),
                Criteria.where("phone").regex(q, "i"),
                Criteria.where("address").regex(q, "i"),
                Criteria.where("title").regex(q, "i"),
                Criteria.where("description").regex(q, "i"),
                Criteria.where("category").regex(q, "i")
        );

        Query query = new Query(c)
                .with(Sort.by(Sort.Direction.DESC, "postedAt"))
                .limit(50);

        List<PostDto> posts = mongoTemplate.find(query, Post.class).stream()
                .map(this::toDto)
                .toList();

        // unique business names (case-insensitive)
        java.util.Set<String> uniq = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (PostDto p : posts) {
            if (p.name() != null && !p.name().trim().isEmpty()) uniq.add(p.name().trim());
        }

        return new PostSearchResponse(List.copyOf(uniq), posts);
    }

    private PostDto toDto(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getName(),
                post.getEmail(),
                post.getPhone(),
                post.getAddress(),
                post.getPostedAt(),   // postedAt -> createdAt
                post.getPostedBy()
        );
    }
}


