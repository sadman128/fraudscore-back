package com.sajid.fraudscore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;


@Document(collection = "posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    private String id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private String title;
    private String category;
    private String description;
    private LocalDateTime postedAt;
    private String imageFolder;
    private String postedBy; //------


    public List<String> likedPostUsers;
    public int likedCount; // count likedPostUsers
    //public boolean ownLiked; // if likedPostUsers has own username


    public List<String> dislikedPostUsers;
    public int dislikedCount; // count dislikedPostUsers
    //public boolean ownDisliked; // if dislikedPostUsers has own username

    @Transient
    private boolean ownLiked = false;

    @Transient
    private boolean ownDisliked = false;

    @Transient
    private boolean ownPosted = false;
}
