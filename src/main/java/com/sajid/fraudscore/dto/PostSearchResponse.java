package com.sajid.fraudscore.dto;

import java.util.List;

public record PostSearchResponse(
        List<String> businessNames,
        List<PostDto> posts
) {}

