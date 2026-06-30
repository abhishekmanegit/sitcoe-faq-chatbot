package com.faqsit.controller;

import com.faqsit.model.ChatRequest;
import com.faqsit.model.ChatResponse;
import com.faqsit.service.GroqService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // fine for local/learning use; restrict this in real production
public class ChatController {

    private final GroqService groqService;

    public ChatController(GroqService groqService) {
        this.groqService = groqService;
    }

    // Frontend calls: POST /api/chat  with body { "question": "..." }
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = groqService.getAnswer(request.getQuestion());
        return new ChatResponse(answer);
    }

    // Simple health check - useful to confirm the server is up
    @GetMapping("/health")
    public String health() {
        return "FAQ Chatbot backend is running";
    }
}
