package com.faqsit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private final RestTemplate restTemplate;

    // Pulled from application.properties / environment variable so the key is never hardcoded
    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    // This is what makes it an "FAQ" bot instead of a generic chatbot -
    // we tell the model what it's allowed to talk about and give it the facts to answer from.
    private static final String SYSTEM_PROMPT = """
        You are the official FAQ assistant for Sharad Institute of Technology
        and College of Engineering (SITCOE), Yadrav.
        Answer student questions ONLY using the facts below. If something is
        not covered here, politely say you don't have that information and
        suggest contacting the college office.

        COLLEGE FACTS:
        - WiFi password: sit@123
        - Gym: Located next to the canteen, near the botanical garden.
        - Courses offered: Computer Science (CS), Electronics and
          Communication (ECE), AI & Data Science (AIDS), Mechanical,
          Mechatronics.
        - Canteen timings: 9 AM to 4 PM.

        Keep answers short (2-4 sentences) and friendly.
        """;

    public GroqService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAnswer(String userQuestion) {
        // 1. Build headers - Groq needs a Bearer token like most OpenAI-compatible APIs
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 2. Build the request body - same shape as OpenAI's chat completion API
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userQuestion)
                ),
                "temperature", 0.4
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 3. Call Groq and parse the response.
        // Spring auto-converts the JSON response into a Map for us (Jackson under the hood).
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(GROQ_URL, requestEntity, Map.class);

        if (response == null || !response.containsKey("choices")) {
            return "Sorry, I couldn't reach the assistant right now. Please try again.";
        }

        // 4. Navigate response structure: choices[0].message.content
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        return (String) message.get("content");
    }
}
