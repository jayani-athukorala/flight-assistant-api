package se.lexicon.flightbooking_api.assistant.dto;

import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;

import java.util.List;
import java.util.UUID;

public record AssistantChatResponse(
        UUID conversationId,
        String message,
        AssistantResponseType type,
        List<AirportResponseDto> airports,
        List<FlightDto> flights,
        List<FlightSeatDto> availableSeats,
        List<BookingResponseDto> bookings,
        PassengerFormSpec passengerForm,
        boolean requiresConfirmation,
        PendingAssistantAction pendingAction
) {
    public AssistantChatResponse {
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
}
