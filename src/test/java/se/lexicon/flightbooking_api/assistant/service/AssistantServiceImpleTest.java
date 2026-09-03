package se.lexicon.flightbooking_api.assistant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatRequest;
import se.lexicon.flightbooking_api.assistant.dto.AssistantChatResponse;
import se.lexicon.flightbooking_api.assistant.dto.AssistantResponseType;
import se.lexicon.flightbooking_api.assistant.exception.AssistantModelException;
import se.lexicon.flightbooking_api.assistant.gateway.AssistantModelGateway;
import se.lexicon.flightbooking_api.assistant.history.InMemoryConversationStore;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssistantServiceImplTest {

    private AssistantModelGateway modelGateway;
    private AssistantServiceImpl assistantService;

    @BeforeEach
    void setUp() {
        InMemoryConversationStore conversationStore =
                new InMemoryConversationStore(
                        16,
                        Duration.ofMinutes(30)
                );

        modelGateway = mock(AssistantModelGateway.class);

        assistantService = new AssistantServiceImpl(
                conversationStore,
                modelGateway
        );
    }

    @Test
    void returnsModelReply() {
        when(modelGateway.generateReply(anyList()))
                .thenReturn(
                        "Which city would you like to fly from?"
                );

        AssistantChatRequest request =
                new AssistantChatRequest(
                        null,
                        "Help me find a flight"
                );

        AssistantChatResponse response =
                assistantService.chat(
                        request,
                        "anonymous"
                );

        assertThat(response.conversationId()).isNotNull();
        assertThat(response.message())
                .isEqualTo(
                        "Which city would you like to fly from?"
                );
        assertThat(response.type())
                .isEqualTo(AssistantResponseType.TEXT);
        assertThat(response.flights()).isEmpty();
        assertThat(response.requiresConfirmation()).isFalse();
    }

    @Test
    void returnsSafeErrorWhenModelFails() {
        when(modelGateway.generateReply(anyList()))
                .thenThrow(
                        new AssistantModelException(
                                "Provider timeout containing internal details"
                        )
                );

        AssistantChatRequest request =
                new AssistantChatRequest(
                        null,
                        "Find flights to Paris"
                );

        AssistantChatResponse response =
                assistantService.chat(
                        request,
                        "anonymous"
                );

        assertThat(response.type())
                .isEqualTo(AssistantResponseType.ERROR);

        assertThat(response.message())
                .isEqualTo(
                        "The assistant is temporarily unavailable. "
                                + "Please try again shortly."
                );

        assertThat(response.message())
                .doesNotContain("internal details");
    }
}