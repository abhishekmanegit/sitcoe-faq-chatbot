package com.faqsit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private final RestTemplate restTemplate;

    // Pulled from application.properties / environment variable so the key is never hardcoded
    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-120b}")
    private String model;

    // Overridable for tests; production always uses the real Groq endpoint
    @Value("${groq.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqUrl;

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
        - who is managing director:  Anil Bagane
        - what is annual fee : its around 60,000
        - how many departments are there : different departments like CSE,AIDS,ECE,Mechnical,Robotics,Mechtronics are offered

        Keep answers short (2-4 sentences) and friendly.
        """;

    public GroqService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAnswer(String userQuestion) {
        if (userQuestion == null || userQuestion.isBlank()) {
            return "Please type a question first.";
        }

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
        // Any Groq-side failure (bad key, decommissioned model, rate limit, network)
        // throws here - we catch it so /api/chat answers with 200 instead of a 500.
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(groqUrl, requestEntity, Map.class);

            if (response == null || !response.containsKey("choices")) {
                log.warn("Groq response had no choices: {}", response);
                return "Sorry, I couldn't reach the assistant right now. Please try again.";
            }

            // 4. Navigate response structure: choices[0].message.content
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            Object content = message.get("content");

            if (content == null || ((String) content).isBlank()) {
                return "I couldn't generate an answer for that. Please try rephrasing your question.";
            }
            return (String) content;
        } catch (RestClientResponseException e) {
            log.error("Groq API returned HTTP {} (model={}): {}",
                    e.getStatusCode(), model, e.getResponseBodyAsString());
            return friendlyGroqError(e.getStatusCode().value());
        } catch (RestClientException e) {
            log.error("Groq API call failed (model={}): {}", model, e.getMessage());
            return "Sorry, I couldn't reach the assistant right now. Please check the server logs and try again.";
        }
    }

    // Turns Groq's HTTP failures into something a student can actually read,
    // while pointing the developer at the likely misconfiguration.
    private String friendlyGroqError(int status) {
        if (status == 401 || status == 403) {
            return "The assistant is not authorised - GROQ_API_KEY is missing or invalid. Please contact the college office.";
        }
        if (status == 404 || status == 400) {
            return "The assistant is misconfigured (rejected model or request). Please contact the college office.";
        }
        if (status == 429) {
            return "I'm getting too many requests right now. Please wait a moment and try again.";
        }
        return "Sorry, the assistant is unavailable right now. Please try again in a moment.";
    }
}
