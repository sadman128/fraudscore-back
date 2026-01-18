package com.sajid.fraudscore.dto;

public record LoginDto(String usernameOrEmail, String password, boolean rememberMe) {
}
