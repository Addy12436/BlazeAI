package com.ai.secserviceImpl;

import com.razorpay.Order;



import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.secret}")
    private String keySecret;

    public Order createOrder(int amount, String currency, String userId) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // amount in paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", "txn_" + userId);
        orderRequest.put("payment_capture", 1); // auto capture

        return client.orders.create(orderRequest);
    }
}
