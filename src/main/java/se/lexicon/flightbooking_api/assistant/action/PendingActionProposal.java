package se.lexicon.flightbooking_api.assistant.action;

import se.lexicon.flightbooking_api.assistant.dto.PendingActionType;
import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;

public record PendingActionProposal(
        PendingActionType type,
        String description,
        BookingRequestDto bookingRequest,
        Long bookingId
) {
    public static PendingActionProposal createBooking(
            String description,
            BookingRequestDto request
    ) {
        return new PendingActionProposal(
                PendingActionType.CREATE_BOOKING,
                description,
                request,
                null
        );
    }

    public static PendingActionProposal cancelBooking(
            String description,
            Long bookingId
    ) {
        return new PendingActionProposal(
                PendingActionType.CANCEL_BOOKING,
                description,
                null,
                bookingId
        );
    }
}
