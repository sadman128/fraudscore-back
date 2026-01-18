package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.component.JwtService;
import com.sajid.fraudscore.model.Post;
import com.sajid.fraudscore.repository.UserRepository;
import com.sajid.fraudscore.service.PostService;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PostController - REST API endpoints for fraud reports
 * Handles post creation, retrieval, searching, and image serving
 */
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    public final PostService postService;
    public final JwtService jwtService;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    public PostController(PostService postService, JwtService jwtService) {
        this.postService = postService;
        this.jwtService = jwtService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        try {
            // Extract user ID from JWT token
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtService.extractSubject(token);


            Post post = new Post();
            post.setName(name);
            post.setEmail(email);
            post.setPhone(phone);
            post.setAddress(address);
            post.setTitle(title);
            post.setCategory(category);
            post.setDescription(description);

            // Save post and images to database/disk
            Post savedPost = postService.createPost(post, images, userId);

            // Return created post with 201 status
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);

        } catch (IOException e) {
            // Handle image upload errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload images: " + e.getMessage()));
        } catch (Exception e) {
            // Handle other errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to create post: " + e.getMessage()));
        }
    }


    @GetMapping
    public ResponseEntity<List<Post>> getPosts(Authentication auth) {
        String currentUsername = auth != null ? auth.getName() : null;
        List<Post> posts = postService.getAllPosts(currentUsername);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id, Authentication auth) {
        String currentUsername = auth != null ? auth.getName() : null;
        Post post = postService.getPost(id, currentUsername);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<?> getPostImages(@PathVariable String id) {
        Optional<Post> postOpt = postService.getPostById(id);
        if (postOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Post not found"));
        }

        Post post = postOpt.get();
        String folderPath = post.getImageFolder();

        // Return empty list if no images folder exists
        if (folderPath == null || folderPath.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        try {
            Path directory = Paths.get(folderPath);
            if (!Files.exists(directory)) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // ✅ FIXED: Return just the filenames, not full URLs
            // Frontend will construct URLs using post ID + filename
            List<String> imageNames = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())  // Just the filename
                    .collect(Collectors.toList());

            System.out.println("Found " + imageNames.size() + " images in: " + folderPath);
            return ResponseEntity.ok(imageNames);

        } catch (IOException e) {
            System.err.println("Failed to list images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to list images"));
        }
    }

    /**
     * GET /api/posts/{id}/images/{filename} - Serve individual image file
     * Returns the actual image file with correct content type
     */
    @GetMapping("/{id}/images/{filename}")
    public ResponseEntity<?> getImage(
            @PathVariable String id,
            @PathVariable String filename) {
        try {
            Optional<Post> postOpt = postService.getPostById(id);
            if (postOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Post post = postOpt.get();

            // Construct full file path
            Path imagePath = Paths.get(post.getImageFolder()).resolve(filename);

            // Security: Prevent directory traversal
            if (!imagePath.toRealPath().startsWith(Paths.get(uploadPath).toRealPath())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if file exists
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            // Create resource
            Resource resource = new UrlResource(imagePath.toUri());
            String contentType = Files.probeContentType(imagePath);

            // Return image with proper headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(postService.searchPosts(query));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Post>> getPostsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(postService.getPostsByCategory(category));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtService.extractSubject(token);
        Post post = postService.getPostById(id).orElse(null);
        if (post.getPostedBy().equals(userId)) {
            postService.deletePost(id);
            return ResponseEntity.ok(new SuccessResponse("Post deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Are you the author ?"));

    }

    static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    static class SuccessResponse {
        public String message;
        public SuccessResponse(String message) {
            this.message = message;
        }
    }
}
