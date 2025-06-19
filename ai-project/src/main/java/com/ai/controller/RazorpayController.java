package com.ai.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ai.entities.User;
import com.ai.repo.Repo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Controller
@RequestMapping("/api/payment")
public class RazorpayController {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @Autowired
    private Repo userRepository;

    // Show the payment page (Thymeleaf template)
    @GetMapping("/pay")
    public String showPaymentPage(Model model) {
        model.addAttribute("razorpayKey", razorpayKeyId);
        return "razor"; // points to payment.html in templates/
    }

    // Create Razorpay order
    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder() {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpaySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", 35000); // ₹350 in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + UUID.randomUUID());

            Order order = razorpay.orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayKeyId);

            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create order"));
        }
    }

    // Verify payment
    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestBody Map<String, String> payload,
            Authentication authentication) throws Exception {

        String orderId = payload.get("razorpayOrderId");
        String paymentId = payload.get("razorpayPaymentId");
        String signature = payload.get("razorpaySignature");

        String email = authentication.getName(); // Spring Security authenticated email
        String generatedSignature = hmacSHA256(orderId + "|" + paymentId, razorpaySecret);

        Map<String, Object> result = new HashMap<>();

        if (generatedSignature.equals(signature)) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPro(true); // Upgrade user
                userRepository.save(user);

                result.put("success", true);
                result.put("message", "Payment verified. User upgraded to Pro.");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "User not found.");
                return ResponseEntity.status(404).body(result);
            }
        } else {
            result.put("success", false);
            result.put("message", "Invalid signature.");
            return ResponseEntity.badRequest().body(result);
        }
    }

    // Signature generation
    private String hmacSHA256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
    }
}
