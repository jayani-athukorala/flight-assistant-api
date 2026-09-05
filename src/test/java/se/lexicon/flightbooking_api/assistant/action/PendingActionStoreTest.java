package se.lexicon.flightbooking_api.assistant.action;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import se.lexicon.flightbooking_api.assistant.dto.PendingActionType;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingActionStoreTest {

    @Test
    void actionCanBeConsumedOnlyOnceByItsOwner() {
        PendingActionStore store = new PendingActionStore(Duration.ofMinutes(5));
        UUID conversationId = UUID.randomUUID();
        var publicAction = store.save(
                conversationId,
                "user@example.com",
                PendingActionProposal.cancelBooking("Cancel booking 42", 42L));

        var stored = store.consume(
                conversationId,
                publicAction.actionId(),
                "user@example.com");

        assertThat(stored.bookingId()).isEqualTo(42L);
        assertThat(stored.publicAction().type())
                .isEqualTo(PendingActionType.CANCEL_BOOKING);
        assertThatThrownBy(() -> store.consume(
                conversationId,
                publicAction.actionId(),
                "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actionCannotBeConsumedByAnotherOwner() {
        PendingActionStore store = new PendingActionStore(Duration.ofMinutes(5));
        UUID conversationId = UUID.randomUUID();
        var publicAction = store.save(
                conversationId,
                "owner@example.com",
                PendingActionProposal.cancelBooking("Cancel booking 42", 42L));

        assertThatThrownBy(() -> store.consume(
                conversationId,
                publicAction.actionId(),
                "attacker@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
