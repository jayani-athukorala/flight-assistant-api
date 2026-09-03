package se.lexicon.flightbooking_api.assistant.dto;

import java.util.UUID;

public record AssistantChatResponse(

        UUID conversationId,

        String message,

        AssistantResponseType type

) {
}