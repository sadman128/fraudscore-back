package com.sajid.fraudscore.service;

import com.sajid.fraudscore.component.JwtService;
import com.sajid.fraudscore.dto.AuthResponse;
import com.sajid.fraudscore.dto.LoginDto;
import com.sajid.fraudscore.dto.OtpDto;
import com.sajid.fraudscore.dto.RegisterDto;
import com.sajid.fraudscore.model.OtpVerify;
import com.sajid.fraudscore.model.User;
import com.sajid.fraudscore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final TurnstileService turnstileService;
    private final JwtService jwtService;

    private List<User> pendingUserList = new ArrayList<>();
    private Map<String, OtpVerify> pendingOtpList = new HashMap<>();

    public String register(RegisterDto dto) {

        turnstileService.verify(dto.turnstileToken());

        if (userRepository.existsByEmail(dto.email())) {
            return "Email already registered";
        }

        if (userRepository.existsByUsername(dto.username())) {
            return "Username already taken";
        }

        if (userRepository.existsByPhone(dto.phone())) {
            return "Phone number already taken";
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPhone(dto.phone());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setEmailVerified(false);
        user.setRole("USER");

        if (pendingUserList != null) {
            pendingUserList.removeIf(u -> u.getEmail().equals(dto.email()));
        }

        pendingUserList.add(user);

        sendOtp(dto.email());
        return "success";
    }

    public String verifyOtp(OtpDto dto) {
        String email = dto.email();
        OtpVerify otpVerify = pendingOtpList.get(email);

        if (otpVerify == null) {
            return "Email not found";
        }

        if (otpVerify.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingOtpList.remove(email);
            return "OTP expired";
        }

        if (!otpVerify.getOtp().equals(dto.otp())) {
            return "Invalid OTP";
        }

        User user = pendingUserList.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (user == null) {
            return "User not found, register again";
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        pendingUserList.removeIf(u -> u.getEmail().equals(email)); // ✅ Clean up
        pendingOtpList.remove(email);

        return "success";
    }

    private void sendOtp(String email) {
        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        OtpVerify entity = new OtpVerify();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5 minutes

        pendingOtpList.put(email, entity);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("FraudScore Email Verification");
        msg.setText("Your OTP is: " + otp); // ✅ Could be prettier HTML email

        try {
            mailSender.send(msg);
        } catch (Exception e) {
            pendingOtpList.remove(email);
            throw new RuntimeException("Failed to send OTP", e);
        }
    }

    public AuthResponse login(LoginDto dto) {
        User user = userRepository.findByUsername(dto.usernameOrEmail())
                .orElseGet(() ->
                        userRepository.findByEmail(dto.usernameOrEmail())
                                .orElseThrow(() -> new RuntimeException("Invalid credentials"))
                );

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email first");
        }

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return new AuthResponse(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        // 1️⃣ Validate refresh token
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2️⃣ Extract username from refresh token
        String username = jwtService.extractSubject(refreshToken);

        // 3️⃣ Load user from DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4️⃣ Generate new access token
        return jwtService.generateAccessToken(user.getUsername(),  user.getRole());
    }


    public User getUser(String username) {
        return  userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
