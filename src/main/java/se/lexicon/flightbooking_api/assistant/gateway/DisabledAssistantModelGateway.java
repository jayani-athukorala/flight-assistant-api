package se.lexicon.flightbooking_api.assistant.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.history.ChatMessage;
import se.lexicon.flightbooking_api.assistant.history.ChatRole;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "openai",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledAssistantModelGateway
        implements AssistantModelGateway {

    @Override
    public AssistantModelResult generateReply(List<ChatMessage> conversation) {
        ChatMessage lastUserMessage = conversation.stream()
                .filter(message -> message.role() == ChatRole.USER)
                .reduce((first, second) -> second)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Conversation contains no user message"
                        )
                );

        return AssistantModelResult.text(
                "Assistant integration is ready. You said: "
                        + lastUserMessage.content()
        );
    }
}