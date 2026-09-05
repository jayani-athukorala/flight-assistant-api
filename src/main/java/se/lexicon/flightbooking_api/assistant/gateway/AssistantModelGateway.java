package se.lexicon.flightbooking_api.assistant.gateway;

import se.lexicon.flightbooking_api.assistant.history.ChatMessage;

import java.util.List;

public interface AssistantModelGateway {

    AssistantModelResult generateReply(
            List<ChatMessage> conversation
    );
}