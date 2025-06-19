package com.ai.service;


import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private Map<String, String> cachedResponses = new HashMap<>();  // Cache for storing responses


    private final UsageLimitService usageLimitService;

    public GeminiService(UsageLimitService usageLimitService) {
        this.usageLimitService = usageLimitService;
    }

  

    public String generateResponse(String prompt,String userId) {
        if (!usageLimitService.canGenerateWebsite(userId)) {
            return "Limit exceeded. Upgrade to Pro version.";
        }
        // Check if the response is cached
        if (cachedResponses.containsKey(prompt)) {
            return cachedResponses.get(prompt);
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String jsonBody = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "%s"
                }
              ]
            }
          ]
        }
        """.formatted(prompt);

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                JSONArray candidates = json.getJSONArray("candidates");
                String generatedText = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                // Cache the response for future use
                cachedResponses.put(prompt, generatedText);
                return generatedText;
            } else {
                return "API Error: " + response.code();
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
}
