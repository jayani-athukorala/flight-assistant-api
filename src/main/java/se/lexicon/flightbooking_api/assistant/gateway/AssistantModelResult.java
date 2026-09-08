package se.lexicon.flightbooking_api.assistant.gateway;

import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.assistant.action.PendingActionProposal;
import se.lexicon.flightbooking_api.assistant.dto.PassengerFormSpec;

import java.util.List;

public record AssistantModelResult(
        String message,
        List<AirportResponseDto> airports,
        List<FlightDto> flights,
        List<FlightSeatDto> availableSeats,
        List<BookingResponseDto> bookings,
        PassengerFormSpec passengerForm,
        PendingActionProposal pendingAction,
        boolean authenticationRequired
) {
    public AssistantModelResult {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Assistant message is required"
            );
        }

        airports = airports == null
                ? List.of()
                : List.copyOf(airports);

        flights = flights == null
                ? List.of()
                : List.copyOf(flights);

        availableSeats = availableSeats == null
                ? List.of()
                : List.copyOf(availableSeats);

        bookings = bookings == null
                ? List.of()
                : List.copyOf(bookings);
    }

    public static AssistantModelResult text(String message) {
        return new AssistantModelResult(
                message,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                false
        );
    }
}
