package se.lexicon.flightbooking_api.assistant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;
import se.lexicon.flightbooking_api.assistant.dto.AssistantResponseType;
import se.lexicon.flightbooking_api.assistant.gateway.AssistantModelGateway;
import se.lexicon.flightbooking_api.assistant.history.ChatMessage;
import se.lexicon.flightbooking_api.assistant.history.ChatRole;
import se.lexicon.flightbooking_api.assistant.history.InMemoryConversationStore;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final InMemoryConversationStore conversationStore;
    private final AssistantModelGateway modelGateway;

    @Override
    public AssistantChatResponse chat(
            AssistantChatRequest request,
            String ownerKey
    ) {
        UUID conversationId = request.conversationId() == null
                ? UUID.randomUUID()
                : request.conversationId();

        conversationStore.append(
                conversationId,
                ownerKey,
                new ChatMessage(
                        ChatRole.USER,
                        request.message()
                )
        );

        List<ChatMessage> conversation =
                conversationStore.getMessages(
                        conversationId,
                        ownerKey
                );

        String assistantMessage =
                modelGateway.generateReply(conversation);

        conversationStore.append(
                conversationId,
                ownerKey,
                new ChatMessage(
                        ChatRole.ASSISTANT,
                        assistantMessage
                )
        );

        return new AssistantChatResponse(
                conversationId,
                assistantMessage,
                AssistantResponseType.TEXT,
                List.of(),
                List.of(),
                List.of(),
                false,
                null
        );
    }
}