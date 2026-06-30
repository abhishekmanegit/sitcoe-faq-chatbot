# SITCOE FAQ Chatbot

Spring Boot + HTML/CSS/JS chatbot that answers college FAQs using the Groq API.

## How to run

1. Get a free Groq API key from https://console.groq.com (you already have one from StudentAgent, reuse it).
2. Set it as an environment variable in your terminal:
   - Windows (cmd): `set GROQ_API_KEY=your_key_here`
   - Windows (PowerShell): `$env:GROQ_API_KEY="your_key_here"`
   - Mac/Linux: `export GROQ_API_KEY=your_key_here`
3. From the project folder, run: `mvn spring-boot:run`
4. Open `http://localhost:8080` in your browser - the chat UI loads automatically.

## How it works (read this before any interview)

**Flow:** Browser (index.html) → POST /api/chat → ChatController → GroqService → Groq API → answer flows back the same path.

- `FaqSitApplication.java` - app entry point. Also defines a `RestTemplate` bean (the tool Spring uses to make outgoing HTTP calls to Groq).
- `ChatRequest.java` / `ChatResponse.java` - plain DTOs (Data Transfer Objects). They just describe the shape of the JSON going in (`{"question": "..."}`) and out (`{"answer": "..."}`). Spring auto-converts JSON ↔ Java objects using these.
- `ChatController.java` - the REST layer. Exposes `POST /api/chat`. Its only job is to receive the request, hand it to the service, and return the result. Controllers should stay thin - no business logic here.
- `GroqService.java` - the actual logic:
  1. Builds an HTTP header with your API key as a Bearer token (this is how Groq authenticates you).
  2. Builds a request body with a "system prompt" (instructions telling the AI it's a SITCOE FAQ bot and what facts it knows) plus the user's actual question.
  3. Sends it to Groq's `/chat/completions` endpoint (same API shape as OpenAI - Groq is "OpenAI-compatible").
  4. Parses the JSON response to pull out just the answer text (`choices[0].message.content`).
- `index.html` - plain JS `fetch()` call to your own backend on button click / Enter key. No frontend framework, so it's easy to explain.

## Things you should be ready to explain in an interview

- **Why a system prompt instead of hardcoding FAQ answers?** It's more flexible - you can update the college facts in one string instead of writing if/else logic, and the AI can paraphrase + handle question variations.
- **Why backend calls Groq instead of frontend calling it directly?** Security - your API key would be exposed in browser JS if called from the frontend. Routing through Spring Boot keeps the key server-side only.
- **What's RestTemplate?** Spring's class for making outgoing HTTP requests (here, to Groq's API). It's the synchronous, simpler alternative to WebClient.
- **What happens if Groq API fails?** Currently returns a friendly fallback message - you could extend this with retries or logging (mention this as a "future improvement" if asked).

## Possible upgrades to mention if asked "what would you improve?"
- Add a real FAQ database (PostgreSQL) instead of hardcoded facts in the prompt - same pattern as your DevCollab project.
- Add chat history / conversation memory.
- Rate-limit the endpoint to avoid API cost abuse.
- Deploy backend to Render and frontend to Vercel, same as DevCollab.
