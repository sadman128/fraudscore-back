package com.sajid.fraudscore.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Map;

@Service
public class AiService {

    public Map<String, Object> restClientChat(String prompt) {
        try {
            RestClient restClient = RestClient.create();
            ResponseEntity<?> response = restClient.post()
                    .uri("http://127.0.0.1:8000/baseten-ai/prompt")
                    .body(Map.of("prompt", prompt))
                    .retrieve()
                    .toEntity(Map.class);

            // 5️⃣ Return Map
            return response != null ? (Map<String, Object>) response.getBody() : Map.of();
        } catch (RestClientException e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }




}
