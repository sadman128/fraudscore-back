package com.sajid.fraudscore.dto;

import java.time.LocalDateTime;

public record PostDto(
        String id,
        String title,
        String description,
        String name,
        String email,
        String phone,
        String address,
        LocalDateTime createdAt,
        String postedBy
) {}
