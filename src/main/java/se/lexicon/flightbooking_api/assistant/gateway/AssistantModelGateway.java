package se.lexicon.flightbooking_api.assistant.gateway;

import se.lexicon.flightbooking_api.assistant.history.ChatMessage;

import java.util.List;

public interface AssistantModelGateway {

    String generateReply(List<ChatMessage> conversation);
}