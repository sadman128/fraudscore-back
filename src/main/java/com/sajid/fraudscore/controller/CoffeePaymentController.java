package com.sajid.fraudscore.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sajid.fraudscore.component.JwtService;
import com.sajid.fraudscore.model.Payment;
import com.sajid.fraudscore.repository.PaymentRepository;
import com.sajid.fraudscore.repository.UserRepository;
import com.sajid.fraudscore.service.CoffeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/coffee")
public class CoffeePaymentController {

    private final PaymentRepository paymentRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CoffeeService coffeeService;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${ssl.baseUrl}")
    private String baseUrl;
    @org.springframework.beans.factory.annotation.Value("${ssl.storeId}")
    private String storeId;
    @org.springframework.beans.factory.annotation.Value("${ssl.storePass}")
    private String storePass;
    @org.springframework.beans.factory.annotation.Value("${app.backendBaseUrl}")
    private String backendBaseUrl;

    public CoffeePaymentController(PaymentRepository paymentRepository, JwtService jwtService, UserRepository userRepository, CoffeeService coffeeService) {
        this.paymentRepository = paymentRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.coffeeService = coffeeService;
    }

    // 1) Frontend calls this -> redirect to returned gatewayUrl
    @PostMapping("/init")
    public Map<String, String> init(
            @RequestBody InitReq req,
            @RequestHeader("Authorization") String auth
    ) throws Exception {

        String token = auth != null && auth.startsWith("Bearer ")
                ? auth.substring(7)
                : auth;

        String username = jwtService.extractSubject(token);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email missing for: " + username);
        }

        String tranId = "COFFEE_" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("store_id", storeId);
        form.add("store_passwd", storePass);
        form.add("total_amount", req.amount.toPlainString());
        form.add("currency", "BDT");
        form.add("tran_id", tranId);

        form.add("success_url", backendBaseUrl + "/api/coffee/success");
        form.add("fail_url", backendBaseUrl + "/api/coffee/fail");
        form.add("cancel_url", backendBaseUrl + "/api/coffee/cancel");
        form.add("ipn_url", backendBaseUrl + "/api/coffee/ipn");

        form.add("product_name", "Buy me a coffee");
        form.add("product_category", "donation");
        form.add("shipping_method", "NO");
        form.add("product_profile", "non-physical-goods");

        // ✅ Required customer fields for SSLCOMMERZ init
        form.add("cus_name", username);
        form.add("cus_email", email.trim());
        form.add("cus_add1", "Dhaka");
        form.add("cus_city", "Dhaka");
        form.add("cus_country", "Bangladesh");
        form.add("cus_phone", "01700000000");

        // Carry your metadata
        form.add("value_a", username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String url = baseUrl + "/gwprocess/v4/api.php";
        ResponseEntity<String> resp =
                rest.exchange(url, HttpMethod.POST, new HttpEntity<>(form, headers), String.class);

        Map<String, Object> body = om.readValue(resp.getBody(), new TypeReference<>() {});
        if (!"SUCCESS".equalsIgnoreCase(String.valueOf(body.get("status")))) {
            throw new RuntimeException("SSL init failed: " + body);
        }

        return Map.of(
                "tranId", tranId,
                "gatewayUrl", String.valueOf(body.get("GatewayPageURL"))
        );
    }

    private void handlePaymentCallback(MultiValueMap<String, String> form) throws Exception {
        String tranId = form.getFirst("tran_id");
        String valId = form.getFirst("val_id");

        if (tranId == null || valId == null) {
            System.out.println("❌ Missing tran_id or val_id: " + form);
            return;
        }

        // Validate with SSLCOMMERZ validation API
        String vUrl = baseUrl + "/validator/api/validationserverAPI.php"
                + "?val_id=" + UriUtils.encode(valId, java.nio.charset.StandardCharsets.UTF_8)
                + "&store_id=" + UriUtils.encode(storeId, java.nio.charset.StandardCharsets.UTF_8)
                + "&store_passwd=" + UriUtils.encode(storePass, java.nio.charset.StandardCharsets.UTF_8)
                + "&format=json";

        String validationResponse = rest.getForObject(vUrl, String.class);
        System.out.println("🔍 SSLCOMMERZ validation response: " + validationResponse);

        Map<String, Object> v = om.readValue(validationResponse, new TypeReference<>() {});

        String vStatus = String.valueOf(v.getOrDefault("status", "")).toUpperCase();
        String vTranId = String.valueOf(v.getOrDefault("tran_id", ""));

        // ✅ SAFE BigDecimal creation
        String amountStr = String.valueOf(v.getOrDefault("amount", "0"));
        if (amountStr == null || amountStr.trim().isEmpty() || "null".equals(amountStr)) {
            amountStr = "0";
        }
        BigDecimal vAmount = new BigDecimal(amountStr);

        String username = String.valueOf(v.getOrDefault("value_a", form.getFirst("value_a")));

        // Save if VALIDATED and IDs match
        if ("VALIDATED".equals(vStatus) && tranId.equals(vTranId)) {
            Payment payment = Payment.builder()
                    .username(username)
                    .amount(vAmount)
                    .time(Instant.now())
                    .build();
            paymentRepository.save(payment);
            System.out.println("✅ Saved coffee payment: " + username + " - ৳" + vAmount);
        } else {
            System.out.println("❌ Payment validation failed: status=" + vStatus + ", expected VALIDATED, tran_id=" + vTranId);
        }
    }

    // 2) SSLCOMMERZ server-to-server notification (primary save location)
    @PostMapping("/ipn")
    public ResponseEntity<String> ipn(@RequestParam MultiValueMap<String, String> form) throws Exception {
        handlePaymentCallback(form);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/success")
    @GetMapping("/success")
    public ResponseEntity<Void> success(@RequestParam MultiValueMap<String, String> form) {
        String tranId = form.getFirst("tran_id");
        String valId = form.getFirst("val_id");
        String amountStr = form.getFirst("amount");
        String username = form.getFirst("value_a");

        if (tranId != null && valId != null && amountStr != null && !amountStr.isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                Payment payment = Payment.builder()
                        .username(username != null ? username : "anonymous")
                        .amount(amount)
                        .time(Instant.now())
                        .tranId(tranId)
                        .valId(valId)
                        .build();
                paymentRepository.save(payment);
                System.out.println("✅ Saved coffee: " + username + " - ৳" + amount);
            } catch (Exception e) {
                System.err.println("Save failed: " + e.getMessage());
            }
        }

        // Force redirect
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:5173?coffee=success"));
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }


    @PostMapping("/fail")
    @GetMapping("/fail")
    public ResponseEntity<Void> fail(@RequestParam MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:5173?coffee=fail"));
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }

    @PostMapping("/cancel")
    @GetMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:5173?coffee=cancel"));
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments() {
        List<Payment> payments = coffeeService.getRecentPayments();
        return ResponseEntity.ok(payments);
    }


    public static class InitReq {
        public BigDecimal amount;
        // username comes from JWT header, not body
    }



}
