package com.sajid.fraudscore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;

    private String username;
    private String email;
    private String phone;
    private String password;
    private boolean emailVerified;

    private String role;
    private String status = "ACTIVE";

    private LocalDateTime createdAt;
}
