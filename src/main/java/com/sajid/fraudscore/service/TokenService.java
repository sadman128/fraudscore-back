package com.sajid.fraudscore.service;

import com.sajid.fraudscore.component.JwtService;
import com.sajid.fraudscore.dto.VerifyTokenDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {
    public final JwtService jwtService;
    public final AuthService authService;

    public TokenService(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    public ResponseEntity<?> verifyToken(VerifyTokenDto verifyTokenDto) {
        if(jwtService.isTokenValid(verifyTokenDto.refreshToken())){

            if(jwtService.isTokenValid(verifyTokenDto.accessToken())){
                Map<String,String> body = new HashMap<>();
                body.put("refresh_token",verifyTokenDto.refreshToken());
                body.put("accessToken",verifyTokenDto.accessToken());
                body.put("username",jwtService.extractSubject(verifyTokenDto.accessToken()));
                IO.println("--------------------verifying refresh token--------------------");
                return ResponseEntity.ok(body);
            }
            else {
                String subject = jwtService.extractSubject(verifyTokenDto.refreshToken());
                String role = authService.getUser(subject).getRole();
                String accessToken = jwtService.generateAccessToken(subject,role);
                Map<String,String> body = new HashMap<>();
                body.put("refresh_token",verifyTokenDto.refreshToken());
                body.put("accessToken", accessToken);
                body.put("username", subject);
                IO.println("--------------------verifying access token--------------------");
                return ResponseEntity.ok(body);
            }

        }
        return  ResponseEntity.notFound().build();
    }
}
