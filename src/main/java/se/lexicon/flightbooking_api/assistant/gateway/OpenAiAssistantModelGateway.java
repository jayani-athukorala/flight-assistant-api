package se.lexicon.flightbooking_api.assistant.gateway;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.config.OpenAiProperties;
import se.lexicon.flightbooking_api.assistant.exception.AssistantModelException;
import se.lexicon.flightbooking_api.assistant.history.ChatMessage;
import se.lexicon.flightbooking_api.assistant.history.ChatRole;
import se.lexicon.flightbooking_api.assistant.prompt.AssistantSystemPrompt;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "openai",
        name = "enabled",
        havingValue = "true"
)
public class OpenAiAssistantModelGateway
        implements AssistantModelGateway {

    private final OpenAIClient openAIClient;
    private final OpenAiProperties properties;

    @Override
    public String generateReply(List<ChatMessage> conversation) {
        try {
            return requestReply(conversation);
        } catch (AssistantModelException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AssistantModelException(
                    "OpenAI request failed",
                    exception
            );
        }
    }

    private String requestReply(List<ChatMessage> conversation) {
        List<ResponseInputItem> inputItems = conversation.stream()
                .filter(message -> message.role() != ChatRole.TOOL)
                .map(this::toInputItem)
                .toList();

        ResponseCreateParams parameters =
                ResponseCreateParams.builder()
                        .model(properties.model())
                        .instructions(AssistantSystemPrompt.PROMPT)
                        .inputOfResponse(inputItems)
                        .build();

        Response response =
                openAIClient.responses().create(parameters);

        String reply = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text().trim())
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining("\n"));

        if (reply.isBlank()) {
            throw new AssistantModelException(
                    "OpenAI returned no assistant text"
            );
        }

        return reply;
    }

    private ResponseInputItem toInputItem(ChatMessage message) {
        EasyInputMessage.Role role =
                message.role() == ChatRole.ASSISTANT
                        ? EasyInputMessage.Role.ASSISTANT
                        : EasyInputMessage.Role.USER;

        EasyInputMessage inputMessage =
                EasyInputMessage.builder()
                        .role(role)
                        .content(message.content())
                        .build();

        return ResponseInputItem.ofEasyInputMessage(inputMessage);
    }
}
