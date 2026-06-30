package com.faqsit.controller;

import com.faqsit.model.ChatRequest;
import com.faqsit.model.ChatResponse;
import com.faqsit.service.GroqService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private final GroqService groqService;

    public ChatController(GroqService groqService) {
        this.groqService = groqService;
    }


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
