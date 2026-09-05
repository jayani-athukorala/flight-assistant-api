package se.lexicon.flightbooking_api.assistant.gateway;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.exception.AssistantModelException;
import se.lexicon.flightbooking_api.assistant.history.ChatMessage;
import se.lexicon.flightbooking_api.assistant.history.ChatRole;
import se.lexicon.flightbooking_api.assistant.prompt.AssistantSystemPrompt;
import se.lexicon.flightbooking_api.assistant.tool.AssistantToolResults;
import se.lexicon.flightbooking_api.assistant.tool.FlightAssistantTools;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "openai", name = "enabled", havingValue = "true")
public class SpringAiAssistantModelGateway implements AssistantModelGateway {

    private final ChatClient chatClient;
    private final AssistantToolResults results;

    public SpringAiAssistantModelGateway(
            ChatClient.Builder builder,
            FlightAssistantTools tools,
            AssistantToolResults results) {
        this.results = results;
        this.chatClient = builder
                .defaultSystem(AssistantSystemPrompt.PROMPT)
                .defaultTools(tools)
                .build();
    }

    @Override
    public AssistantModelResult generateReply(List<ChatMessage> conversation) {
        results.begin();
        try {
            List<Message> messages = conversation.stream()
                    .filter(item -> item.role() != ChatRole.TOOL)
                    .map(this::toSpringMessage)
                    .toList();

            String content = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            if (content == null || content.isBlank()) {
                throw new AssistantModelException("OpenAI returned no assistant text");
            }
            return results.complete(content);
        } catch (AssistantModelException exception) {
            results.clear();
            throw exception;
        } catch (RuntimeException exception) {
            results.clear();
            throw new AssistantModelException("Spring AI request failed", exception);
        }
    }

    private Message toSpringMessage(ChatMessage message) {
        return message.role() == ChatRole.ASSISTANT
                ? AssistantMessage.builder().content(message.content()).build()
                : UserMessage.builder().text(message.content()).build();
    }
}
