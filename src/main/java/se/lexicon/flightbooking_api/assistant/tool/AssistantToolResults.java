package se.lexicon.flightbooking_api.assistant.tool;

import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.action.PendingActionProposal;
import se.lexicon.flightbooking_api.assistant.gateway.AssistantModelResult;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;

import java.util.ArrayList;
import java.util.List;

/** Collects structured tool output for the current synchronous ChatClient call. */
@Component
public class AssistantToolResults {

    private final ThreadLocal<State> current = new ThreadLocal<>();

    public void begin() { current.set(new State()); }
    public void airports(List<AirportResponseDto> value) { state().airports.addAll(value); }
    public void flights(List<FlightDto> value) { state().flights.addAll(value); }
    public void seats(List<FlightSeatDto> value) { state().seats.addAll(value); }
    public void bookings(List<BookingResponseDto> value) { state().bookings.addAll(value); }
    public void pending(PendingActionProposal value) { state().pendingAction = value; }
    public void authenticationRequired() { state().authenticationRequired = true; }

    public AssistantModelResult complete(String message) {
        State value = state();
        try {
            return new AssistantModelResult(message, value.airports, value.flights,
                    value.seats, value.bookings, value.pendingAction,
                    value.authenticationRequired);
        } finally {
            current.remove();
        }
    }

    public void clear() { current.remove(); }

    private State state() {
        State value = current.get();
        if (value == null) throw new IllegalStateException("No active assistant request");
        return value;
    }

    private static final class State {
        private final List<AirportResponseDto> airports = new ArrayList<>();
        private final List<FlightDto> flights = new ArrayList<>();
        private final List<FlightSeatDto> seats = new ArrayList<>();
        private final List<BookingResponseDto> bookings = new ArrayList<>();
        private PendingActionProposal pendingAction;
        private boolean authenticationRequired;
    }
}
