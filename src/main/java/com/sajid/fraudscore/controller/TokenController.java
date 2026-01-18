package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.dto.RefreshTokenDto;
import com.sajid.fraudscore.dto.VerifyTokenDto;
import com.sajid.fraudscore.service.AuthService;
import com.sajid.fraudscore.service.TokenService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TokenController {
    public final AuthService authService;
    public final TokenService tokenService;

    public TokenController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }
    @PostMapping("/verifyLogin")
    public ResponseEntity<?> verifyToken(
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        try {

            String accessToken = authorization.replace("Bearer ", "").trim();
            IO.println("\n");
            IO.println("AccessToken: " + accessToken + LocalDate.now());
            IO.println("RefreshToken: " + refreshToken + LocalDate.now());
            IO.println("\n");

            VerifyTokenDto verifyTokenDto = new VerifyTokenDto(accessToken, refreshToken);
            return tokenService.verifyToken(verifyTokenDto);
        } catch (Exception e) {
            IO.println("Verify token error: " + e.getMessage());
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid token", "error", e.getMessage()));
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenDto dto) {
        try {
            String newAccessToken = authService.refreshAccessToken(dto.refreshToken());
            return ResponseEntity.ok(
                    Map.of("accessToken", newAccessToken)
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
