package com.sajid.fraudscore.service;

import com.sajid.fraudscore.model.Post;
import com.sajid.fraudscore.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    public Post createPost(Post post, List<MultipartFile> images, String userId) throws IOException {
        // Generate unique ID for this post
        String postId = UUID.randomUUID().toString();
        post.setId(postId);
        post.setPostedAt(LocalDateTime.now());
        post.setPostedBy(userId);

        // **FIX: Initialize reaction lists to prevent NPE in getAllPosts**
        post.setLikedPostUsers(new ArrayList<>());
        post.setDislikedPostUsers(new ArrayList<>());

        // Create folder for images if any are provided
        if (images != null && !images.isEmpty()) {
            String folderPath = uploadPath + "/posts/" + postId;
            Path directory = Paths.get(folderPath);

            // Create directory if it doesn't exist
            Files.createDirectories(directory);

            // Save each image to the folder
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                if (!image.isEmpty()) {
                    // Create unique filename with index + extension
                    String originalName = image.getOriginalFilename();
                    String extension = originalName != null && originalName.contains(".")
                            ? originalName.substring(originalName.lastIndexOf("."))
                            : ".jpg";
                    String fileName = "image_" + (i + 1) + extension;

                    Path filePath = directory.resolve(fileName);
                    // Copy image file to disk
                    Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            // Store folder path in post document
            post.setImageFolder(folderPath);
        }

        // Save post to database and return
        return postRepository.save(post);
    }



    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }

    public List<Post> getPostsByUser(String userId) {
        return postRepository.findByPostedBy(userId);
    }


    public List<Post> searchPosts(String query) {
        return postRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
                query, query, query);
    }


    public List<Post> getPostsByCategory(String category) {
        return postRepository.findByCategory(category);
    }


    public void deletePost(String id) {
        postRepository.deleteById(id);
        // image folder cleanup could be added here if needed
    }

    public List<Post> getAllPosts(String currentUsername) {
        IO.println("gettin all post for : " + currentUsername);
        List<Post> posts = postRepository.findAllByOrderByPostedAtDesc(); // or your query
        return enrichWithUserReactions(posts, currentUsername);
    }

    public Post getPost(String id, String currentUsername) {
        Post post = postRepository.findById(id).orElse(null);
        return enrichWithUserReactions(List.of(post), currentUsername)
                .stream().findFirst().orElse(null);
    }

    private List<Post> enrichWithUserReactions(List<Post> posts, String currentUsername) {
        if (currentUsername == null) {
            // Guest user: no green/red states and not own post
            posts.forEach(post -> {
                post.setOwnLiked(false);
                post.setOwnDisliked(false);
                post.setOwnPosted(false);
            });
            return posts;
        }

        return posts.stream().map(post -> {
            post.setOwnLiked(post.getLikedPostUsers().contains(currentUsername));
            post.setOwnDisliked(post.getDislikedPostUsers().contains(currentUsername));
            post.setOwnPosted(currentUsername.equals(post.getPostedBy()));

            return post;
        }).collect(Collectors.toList());
    }



}
