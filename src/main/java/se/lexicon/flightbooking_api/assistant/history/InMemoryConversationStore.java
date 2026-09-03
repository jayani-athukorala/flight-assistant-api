package se.lexicon.flightbooking_api.assistant.history;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryConversationStore {

    private final ConcurrentHashMap<UUID, ConversationState> conversations =
            new ConcurrentHashMap<>();

    private final int maxMessages;
    private final Duration timeToLive;

    public InMemoryConversationStore(
            @Value("${assistant.history.max-messages:16}")
            int maxMessages,

            @Value("${assistant.history.ttl:30m}")
            Duration timeToLive
    ) {
        if (maxMessages < 2) {
            throw new IllegalArgumentException(
                    "assistant.history.max-messages must be at least 2"
            );
        }

        if (timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException(
                    "assistant.history.ttl must be positive"
            );
        }

        this.maxMessages = maxMessages;
        this.timeToLive = timeToLive;
    }

    public void append(
            UUID conversationId,
            String ownerKey,
            ChatMessage message
    ) {
        requireArguments(conversationId, ownerKey, message);

        Instant now = Instant.now();

        ConversationState state = conversations.compute(
                conversationId,
                (id, existing) -> {
                    if (existing == null || existing.isExpired(now)) {
                        return new ConversationState(
                                ownerKey,
                                now.plus(timeToLive)
                        );
                    }

                    verifyOwner(existing, ownerKey);
                    existing.extendExpiry(now.plus(timeToLive));
                    return existing;
                }
        );

        synchronized (state) {
            state.messages.addLast(message);

            while (state.messages.size() > maxMessages) {
                state.messages.removeFirst();
            }
        }
    }

    public List<ChatMessage> getMessages(
            UUID conversationId,
            String ownerKey
    ) {
        if (conversationId == null) {
            throw new IllegalArgumentException(
                    "Conversation ID is required"
            );
        }

        if (ownerKey == null || ownerKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Conversation owner is required"
            );
        }

        ConversationState state = conversations.get(conversationId);

        if (state == null) {
            return List.of();
        }

        Instant now = Instant.now();

        if (state.isExpired(now)) {
            conversations.remove(conversationId, state);
            return List.of();
        }

        verifyOwner(state, ownerKey);

        synchronized (state) {
            return List.copyOf(new ArrayList<>(state.messages));
        }
    }

    public void clear(
            UUID conversationId,
            String ownerKey
    ) {
        if (conversationId == null) {
            throw new IllegalArgumentException(
                    "Conversation ID is required"
            );
        }

        if (ownerKey == null || ownerKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Conversation owner is required"
            );
        }

        ConversationState state = conversations.get(conversationId);

        if (state == null) {
            return;
        }

        verifyOwner(state, ownerKey);
        conversations.remove(conversationId, state);
    }

    public void removeExpiredConversations() {
        Instant now = Instant.now();

        conversations.entrySet().removeIf(
                entry -> entry.getValue().isExpired(now)
        );
    }

    private void verifyOwner(
            ConversationState state,
            String ownerKey
    ) {
        if (!state.ownerKey.equals(ownerKey)) {
            throw new AccessDeniedException(
                    "Conversation does not belong to this user"
            );
        }
    }

    private void requireArguments(
            UUID conversationId,
            String ownerKey,
            ChatMessage message
    ) {
        if (conversationId == null) {
            throw new IllegalArgumentException(
                    "Conversation ID is required"
            );
        }

        if (ownerKey == null || ownerKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Conversation owner is required"
            );
        }

        if (message == null) {
            throw new IllegalArgumentException(
                    "Chat message is required"
            );
        }
    }

    private static final class ConversationState {

        private final String ownerKey;
        private final Deque<ChatMessage> messages = new ArrayDeque<>();
        private volatile Instant expiresAt;

        private ConversationState(
                String ownerKey,
                Instant expiresAt
        ) {
            this.ownerKey = ownerKey;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }

        private void extendExpiry(Instant newExpiry) {
            this.expiresAt = newExpiry;
        }
    }

}