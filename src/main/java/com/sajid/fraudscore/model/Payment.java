package com.sajid.fraudscore.model;

// Donation.java
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id private String id;
    private String username;
    private BigDecimal amount;
    private String tranId;
    private String valId;
    private Instant time;
}

