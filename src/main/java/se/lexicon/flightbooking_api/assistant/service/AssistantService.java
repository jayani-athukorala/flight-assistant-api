package se.lexicon.flightbooking_api.assistant.service;

import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;

import java.util.UUID;

public interface AssistantService {

    AssistantChatResponse chat(
            AssistantChatRequest request,
            String ownerKey
    );

    AssistantChatResponse confirmAction(
            UUID conversationId,
            UUID actionId,
            String ownerKey
    );

    void rejectAction(UUID conversationId, UUID actionId, String ownerKey);

    void clearConversation(UUID conversationId, String ownerKey);
}
