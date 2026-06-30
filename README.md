# SITCOE FAQ Chatbot 🤖

An AI-powered FAQ chatbot built for Sharad Institute of Technology and College of Engineering (SITCOE) that answers student queries about courses, campus facilities, and college information using natural language.

## 🚀 Features

- Conversational chat interface — ask questions in plain English
- AI-powered responses using Groq's LLM API
- Custom knowledge base covering courses, WiFi, gym, canteen timings, and more
- Lightweight, fast, and easy to extend with new FAQs

## 🛠️ Tech Stack

- **Backend:** Java, Spring Boot, REST API
- **AI:** Groq API (LLM-based responses)
- **Frontend:** HTML, CSS, JavaScript
- **Build Tool:** Maven

## 📸 Screenshot


<img width="1137" height="850" alt="Screenshot 2026-06-30 183100" src="https://github.com/user-attachments/assets/d66848b9-439a-438f-83d6-9900f5ff0a06" />

## ⚙️ How It Works

1. User types a question in the chat UI.
2. Frontend sends the question to the Spring Boot backend via a REST endpoint (`/api/chat`).
3. Backend forwards the query to Groq's API along with a system prompt containing SITCOE-specific facts.
4. AI generates a contextual response, which is sent back and displayed in the chat.

## 🏃 Run Locally

1. Clone the repo
git clone https://github.com/abhishekmanegit/sitcoe-faq-chatbot.git
2. Set your Groq API key as an environment variable
GROQ_API_KEY=your_key_here
3. Run the app
mvn spring-boot:run
4. Open `http://localhost:8080` in your browser

## 📂 Project Structure
src/main/java/com/faqsit/
├── controller/    → REST API endpoint
├── service/       → Groq API integration & prompt logic
├── model/         → Request/Response DTOs
└── resources/
└── static/    → Frontend (HTML/CSS/JS)

## ✨ Future Improvements

- Database-backed FAQ storage instead of hardcoded prompt
- Chat history / conversation memory
- Admin panel to update FAQs without code changes
