package se.lexicon.flightbooking_api.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;
import se.lexicon.flightbooking_api.assistant.dto.AssistantResponseType;
import se.lexicon.flightbooking_api.assistant.exception.AssistantModelException;
import se.lexicon.flightbooking_api.assistant.gateway.AssistantModelGateway;
import se.lexicon.flightbooking_api.assistant.history.ChatMessage;
import se.lexicon.flightbooking_api.assistant.history.ChatRole;
import se.lexicon.flightbooking_api.assistant.history.InMemoryConversationStore;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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

        String assistantMessage;
        AssistantResponseType responseType;

        try {
            assistantMessage =
                    modelGateway.generateReply(conversation);

            responseType = AssistantResponseType.TEXT;
        } catch (AssistantModelException exception) {
            log.error(
                    "Assistant model request failed for conversation {}",
                    conversationId,
                    exception
            );

            assistantMessage =
                    "The assistant is temporarily unavailable. "
                            + "Please try again shortly.";

            responseType = AssistantResponseType.ERROR;
        }

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
                responseType,
                List.of(),
                List.of(),
                List.of(),
                false,
                null
        );
    }
}