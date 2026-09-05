# ✈️ Flight Assistant API

Spring Boot backend for a flight reservation system with an OpenAI-powered assistant. Users can search flights and seats publicly, while authenticated users can create, view, and cancel their own bookings.

## Features

- Airport, flight, and available-seat search
- JWT registration and authentication
- One-way and round-trip bookings
- Booking ownership and cancellation validation
- AI assistant with Spring AI `ChatClient` and `@Tool`
- In-memory, limited conversation history
- Explicit confirmation before booking or cancellation
- Structured assistant responses for the React client
- Swagger/OpenAPI documentation

## Technology

Java 25, Spring Boot, Spring AI, Spring Security, JWT, Spring Data JPA, PostgreSQL, Maven, Docker, Swagger and OpenAI.

## Assistant API

```http
POST /api/assistant/chat
POST /api/assistant/conversations/{conversationId}/actions/{actionId}/confirm
DELETE /api/assistant/conversations/{conversationId}/actions/{actionId}
DELETE /api/assistant/conversations/{conversationId}
```

Searching airports, flights, and seats is public. Personal bookings and booking changes require a valid JWT. Booking and cancellation tools only prepare pending actions; the confirmation endpoint performs the final operation.

## Local setup

Create a `.env` file without committing it:

```env
POSTGRES_PASSWORD=change-me
JWT_SECRET=use-a-long-random-secret
OPENAI_ENABLED=true
OPENAI_API_KEY=your-openai-api-key
OPENAI_MODEL=gpt-5-mini
FRONTEND_URL=http://localhost:5173
```

Run with Docker:

```bash
docker compose up -d --build
```

Run tests:

```bash
./mvnw test
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Important configuration

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  ai:
    openai:
      api-key: ${OPENAI_API_KEY:disabled}

openai:
  enabled: ${OPENAI_ENABLED:false}
```

Conversation history is stored in memory and disappears when the application restarts. Secrets must be configured through environment variables.

## Repository

[github.com/jayani-athukorala/flight-assistant-api](https://github.com/jayani-athukorala/flight-assistant-api)

## Author

Jayani Athukorala
