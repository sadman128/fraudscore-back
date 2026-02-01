package com.sajid.fraudscore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Use patterns (supports wildcards)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://*.ngrok-free.app",
                "https://sandbox.sslcommerz.com",
                "https://securepay.sslcommerz.com"
        )); // wildcards belong here, not in allowedOrigins [web:497]

        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // only if you use cookies/session [web:500]

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

