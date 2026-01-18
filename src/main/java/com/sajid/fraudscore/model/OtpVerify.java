package com.sajid.fraudscore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerify {
    private String email;
    private String otp;
    private LocalDateTime expiresAt;
}
