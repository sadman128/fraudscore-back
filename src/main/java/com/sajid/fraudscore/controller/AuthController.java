package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.dto.AuthResponse;
import com.sajid.fraudscore.dto.LoginDto;
import com.sajid.fraudscore.dto.OtpDto;
import com.sajid.fraudscore.dto.RegisterDto;
import com.sajid.fraudscore.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterDto dto) {
        try {
            String message = authService.register(dto);

            Map<String, String> body = Map.of("message", message);

            return switch (message) {
                case "Email already registered",
                     "Username already taken",
                     "Phone number already taken" ->
                        ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
                case "success" ->
                        ResponseEntity.status(HttpStatus.ACCEPTED).body(body); // 202 - OTP sent
                default ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("message", "Unexpected error"));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpDto dto) {
        try {
            String message = authService.verifyOtp(dto);

            Map<String, String> body = Map.of("message", message);

            return switch (message) {
                case "Email not found",
                     "User not found, register again" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
                case "OTP expired",
                     "Invalid OTP" ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
                case "success" ->
                        ResponseEntity.ok(Map.of("message", "Email verified successfully.")); // 200
                default ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("message", "Unexpected error"));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {

            IO.println("------------login-----------");
            AuthResponse response = authService.login(dto);
            return ResponseEntity.ok(response); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage())); // 400
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage())); // 500
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from "Bearer <token>"
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Missing or invalid Authorization header"));
            }

            String refreshToken = authHeader.substring(7);
            String accessToken = authService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(Map.of("accessToken", accessToken)); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage())); // 401
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage())); // 500
        }
    }

    /**
     * Health check endpoint
     * Response: 200 OK: { status: "Auth service is running" }
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Auth service is running"));
    }
}
