package com.sajid.fraudscore.service;

import com.sajid.fraudscore.model.Payment;
import com.sajid.fraudscore.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoffeeService {
    public final PaymentRepository paymentRepository;

    public CoffeeService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getRecentPayments() {
        List<Payment> payments = paymentRepository.findTop20ByOrderByTimeDesc();
        payments.forEach(payment -> {
            payment.setValId("");   // hide sensitive info
            payment.setTranId("");
        });
        return payments;
    }

}
