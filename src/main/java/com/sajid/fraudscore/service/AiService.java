package com.sajid.fraudscore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sajid.fraudscore.dto.FraudAnalysisDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restClient;

    public AiService(RestTemplate restClient) {
        this.restClient = restClient;
    }


    public Map<String, Object> analyzeFraud(FraudAnalysisDto fraudAnalysisDto) {
        try {

            Map<String, Object> requestBody = Map.of(
                    "entityName", fraudAnalysisDto.getEntityName(),
                    "posts", fraudAnalysisDto.getPosts(),
                    "comments", fraudAnalysisDto.getComments()
            );


            Map<String, Object> responseMap = restClient.postForObject(
                    "http://127.0.0.1:8000/baseten-ai/get-score",
                    requestBody,
                    Map.class
            );

            IO.println(responseMap);
            return responseMap;

        } catch (RestClientException e) {
            e.printStackTrace();

            // Return a map with error info
            return Map.of(
                    "analysis_status", "failed",
                    "summary", "Error: " + e.getMessage()
            );
        }
    }
}
