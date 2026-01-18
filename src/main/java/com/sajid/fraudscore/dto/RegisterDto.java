package com.sajid.fraudscore.dto;

public record RegisterDto (String username, String email, String phone,String password, String turnstileToken) {}
