package se.lexicon.flightbooking_api.assistant.service;

import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;

public interface AssistantService {

    AssistantChatResponse chat(
            AssistantChatRequest request,
            String ownerKey
    );
}