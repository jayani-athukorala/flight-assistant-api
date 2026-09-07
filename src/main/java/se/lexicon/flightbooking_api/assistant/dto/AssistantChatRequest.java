package se.lexicon.flightbooking_api.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AssistantChatRequest(

        /*
         * Null for the first message.z
         * The backend creates a new conversation ID.
         */
        UUID conversationId,

        @NotBlank(
                message = "Message is required"
        )
        @Size(
                max = 2000,
                message = "Message must not exceed 2000 characters"
        )
        String message

) {
}