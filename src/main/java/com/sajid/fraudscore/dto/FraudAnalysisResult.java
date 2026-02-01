package com.sajid.fraudscore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FraudAnalysisResult {

    private String entity_name;
    private String analysis_status; // "completed"

    private FraudAssessment fraud_assessment;
    private String summary;
    private EvidenceAnalysis evidence_analysis;
    private ManipulationAnalysis manipulation_analysis;
    private List<FraudPattern> fraud_patterns;
    private ContentStatistics content_statistics;
    private SentimentBreakdown sentiment_breakdown;
    private List<String> uncertainty_factors;
    private UserGuidance user_guidance;
    private String legal_disclaimer;

    // ------------------------
    @Data
    public static class FraudAssessment {
        private int fraud_risk_score; // 0-100
        private String risk_level;    // "Safe | Warning | Medium | High | Critical"
        private String confidence;    // "Low | Medium | High"
    }

    @Data
    public static class EvidenceAnalysis {
        private List<String> strong_evidence;
        private List<String> weak_evidence;
        private List<String> contradicting_evidence;
        private String evidence_sufficiency; // "Sufficient | Insufficient | Conflicting"
    }

    @Data
    public static class ManipulationAnalysis {
        private boolean suspected_fake_activity;
        private List<String> indicators;
    }

    @Data
    public static class FraudPattern {
        private String pattern_name;
        private String description;
        private String likelihood; // "Low | Medium | High"
    }

    @Data
    public static class ContentStatistics {
        private int total_posts;
        private int posts_with_complaints;
        private int posts_with_positive_feedback;
        private int neutral_posts;
    }

    @Data
    public static class SentimentBreakdown {
        private int positive_percentage;
        private int neutral_percentage;
        private int negative_percentage;
    }

    @Data
    public static class UserGuidance {
        private String recommendation_level; // "Informational | Caution | Avoid"
        private List<String> safety_tips;
    }
}

