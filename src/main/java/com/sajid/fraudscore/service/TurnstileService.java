package com.sajid.fraudscore.service;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


@Service
public class TurnstileService {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verify(String token) {
        String secret = "0x4AAAAAACKpIzTx27slPJpfQ3m05RNz3fE";

        String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secret);
        body.add("response", token);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        Map<?, ?> response =
                restTemplate.postForObject(url, request, Map.class);

        return Boolean.TRUE.equals(response.get("success"));
    }

}

// sob e chatgpt er doa
