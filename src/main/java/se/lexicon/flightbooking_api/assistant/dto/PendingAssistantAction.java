package se.lexicon.flightbooking_api.assistant.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PendingAssistantAction(
        UUID actionId,
        PendingActionType type,
        String description,
        Map<String, Object> parameters,
        Instant expiresAt
) {
}