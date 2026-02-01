package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.dto.FraudAnalysisResult;
import com.sajid.fraudscore.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/score")
//@CrossOrigin(origins = "*")
public class ScoreController {

    public final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping
    public ResponseEntity<?> getScore(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        System.out.println("getting score prompt: " + prompt);

        Map<String, Object> result = scoreService.getScore(prompt);

        return ResponseEntity.ok(result);
    }


}
