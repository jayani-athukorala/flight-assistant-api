package se.lexicon.flightbooking_api.assistant.history;

import java.time.Instant;

public record ChatMessage(
        ChatRole role,
        String content,
        Instant createdAt
) {
    public ChatMessage(ChatRole role, String content) {
        this(role, content, Instant.now());
    }
}