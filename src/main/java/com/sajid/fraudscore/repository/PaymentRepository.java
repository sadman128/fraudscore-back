package com.sajid.fraudscore.repository;

import com.sajid.fraudscore.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findTop20ByOrderByTimeDesc();
}

