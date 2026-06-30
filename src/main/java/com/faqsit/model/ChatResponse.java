package com.faqsit.model;

// Represents the JSON we send back to the frontend: { "answer": "..." }
public class ChatResponse {

    private String answer;

    public ChatResponse() {
    }

    public ChatResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
