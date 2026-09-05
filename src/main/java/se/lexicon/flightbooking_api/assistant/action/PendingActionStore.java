package se.lexicon.flightbooking_api.assistant.action;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.dto.PendingAssistantAction;
import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingActionStore {

    private final ConcurrentHashMap<UUID, StoredPendingAction> actions =
            new ConcurrentHashMap<>();
    private final Duration timeToLive;

    public PendingActionStore(
            @Value("${assistant.actions.ttl:5m}") Duration timeToLive
    ) {
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException(
                    "assistant.actions.ttl must be positive"
            );
        }
        this.timeToLive = timeToLive;
    }

    public PendingAssistantAction save(
            UUID conversationId,
            String ownerKey,
            PendingActionProposal proposal
    ) {
        UUID actionId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(timeToLive);
        Map<String, Object> parameters = publicParameters(proposal);

        PendingAssistantAction publicAction = new PendingAssistantAction(
                actionId,
                proposal.type(),
                proposal.description(),
                parameters,
                expiresAt
        );

        actions.put(
                actionId,
                new StoredPendingAction(
                        publicAction,
                        conversationId,
                        ownerKey,
                        proposal.bookingRequest(),
                        proposal.bookingId()
                )
        );
        return publicAction;
    }

    public StoredPendingAction consume(
            UUID conversationId,
            UUID actionId,
            String ownerKey
    ) {
        StoredPendingAction stored = requireAction(actionId);
        verifyOwnership(stored, conversationId, ownerKey);

        if (!stored.publicAction().expiresAt().isAfter(Instant.now())) {
            actions.remove(actionId, stored);
            throw new IllegalArgumentException(
                    "The pending assistant action has expired"
            );
        }

        if (!actions.remove(actionId, stored)) {
            throw new IllegalArgumentException(
                    "The pending assistant action is no longer available"
            );
        }
        return stored;
    }

    public void reject(
            UUID conversationId,
            UUID actionId,
            String ownerKey
    ) {
        StoredPendingAction stored = requireAction(actionId);
        verifyOwnership(stored, conversationId, ownerKey);
        actions.remove(actionId, stored);
    }

    public void clearConversation(UUID conversationId, String ownerKey) {
        actions.entrySet().removeIf(entry -> {
            StoredPendingAction stored = entry.getValue();
            return stored.conversationId().equals(conversationId)
                    && stored.ownerKey().equals(ownerKey);
        });
    }

    private StoredPendingAction requireAction(UUID actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("Action ID is required");
        }
        StoredPendingAction stored = actions.get(actionId);
        if (stored == null) {
            throw new IllegalArgumentException(
                    "Pending assistant action was not found"
            );
        }
        return stored;
    }

    private void verifyOwnership(
            StoredPendingAction stored,
            UUID conversationId,
            String ownerKey
    ) {
        if (!stored.conversationId().equals(conversationId)
                || !stored.ownerKey().equals(ownerKey)) {
            throw new AccessDeniedException(
                    "Pending action does not belong to this conversation"
            );
        }
    }

    private Map<String, Object> publicParameters(
            PendingActionProposal proposal
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (proposal.bookingId() != null) {
            parameters.put("bookingId", proposal.bookingId());
        }
        if (proposal.bookingRequest() != null) {
            BookingRequestDto request = proposal.bookingRequest();
            parameters.put("outboundFlightId", request.outboundFlightId());
            if (request.returnFlightId() != null) {
                parameters.put("returnFlightId", request.returnFlightId());
            }
            parameters.put("passengerCount", request.passengers().size());
        }
        return Map.copyOf(parameters);
    }

    public record StoredPendingAction(
            PendingAssistantAction publicAction,
            UUID conversationId,
            String ownerKey,
            BookingRequestDto bookingRequest,
            Long bookingId
    ) {
    }
}
