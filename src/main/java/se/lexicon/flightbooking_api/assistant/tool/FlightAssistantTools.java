package se.lexicon.flightbooking_api.assistant.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.exception.AssistantAuthenticationRequiredException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The only application operations exposed to the AI model. */
@Component
@RequiredArgsConstructor
public class FlightAssistantTools {

    private final AssistantToolExecutor executor;
    private final AssistantToolResults results;

    @Tool(
            description = """
                Search airports only when the user explicitly asks to find,
                identify or list airports.

                Never call this tool when the user asks to search for flights.
                For flight requests, first collect origin, destination and
                departure date, then call searchFlightsByLocations.
                """
    )
    public Object searchAirports(
            @ToolParam(description = "Airport name, city, country or IATA code")
            String query
    ) {
        var args = new AssistantToolDefinitions.SearchAirports();
        args.query = query;

        return execute(() -> {
            var value = executor.searchAirports(args);
            results.airports(value);
            return value;
        });
    }

    @Tool(description = "Search available flights between two airport database IDs, optionally on an ISO date.")
    public Object searchAvailableFlights(
            @ToolParam(description = "Origin airport database ID") Long originAirportId,
            @ToolParam(description = "Destination airport database ID") Long destinationAirportId,
            @ToolParam(description = "Optional date in YYYY-MM-DD format", required = false) String departureDate) {
        var args = new AssistantToolDefinitions.SearchAvailableFlights();
        args.originAirportId = originAirportId;
        args.destinationAirportId = destinationAirportId;
        args.departureDate = departureDate;
        return execute(() -> {
            var value = executor.searchAvailableFlights(args);
            results.flights(value);
            return value;
        });
    }

    @Tool(description = "Get currently available seats for a flight. This operation is public.")
    public Object getAvailableSeats(
            @ToolParam(description = "Flight database ID") Long flightId,
            @ToolParam(description = "Optional seat class", required = false) String seatClass) {
        var args = new AssistantToolDefinitions.GetAvailableSeats();
        args.flightId = flightId;
        args.seatClass = seatClass;
        return execute(() -> {
            var value = executor.getAvailableSeats(args);
            results.seats(value);
            return value;
        });
    }

    @Tool(description = "Get bookings owned by the authenticated user. Never request an email for authorization.")
    public Object getMyBookings(
            @ToolParam(description = "Whether archived bookings should be returned", required = false) boolean archived) {
        var args = new AssistantToolDefinitions.GetMyBookings();
        args.archived = archived;
        return execute(() -> {
            var value = executor.getMyBookings(args);
            results.bookings(value);
            return value;
        });
    }

    @Tool(description = "Prepare a booking for explicit confirmation. This never creates the booking immediately.")
    public Object createBooking(
            @ToolParam(description = "Outbound flight ID") Long outboundFlightId,
            @ToolParam(description = "Optional return flight ID", required = false) Long returnFlightId,
            @ToolParam(description = "Passengers with selected seat IDs") List<AssistantToolDefinitions.PassengerInput> passengers) {
        var args = new AssistantToolDefinitions.CreateBooking();
        args.outboundFlightId = outboundFlightId;
        args.returnFlightId = returnFlightId;
        args.passengers = passengers;
        return execute(() -> {
            var value = executor.prepareCreateBooking(args);
            results.pending(value);
            return Map.of("status", "confirmation_required", "description", value.description());
        });
    }

    @Tool(description = "Prepare cancellation of an owned booking for explicit confirmation. This never cancels immediately.")
    public Object cancelBooking(@ToolParam(description = "Booking database ID") Long bookingId) {
        var args = new AssistantToolDefinitions.CancelBooking();
        args.bookingId = bookingId;
        return execute(() -> {
            var value = executor.prepareCancelBooking(args);
            results.pending(value);
            return Map.of("status", "confirmation_required", "description", value.description());
        });
    }

    private Object execute(Supplier<Object> operation) {
        try {
            return operation.get();
        } catch (AssistantAuthenticationRequiredException exception) {
            results.authenticationRequired();
            return Map.of("authenticationRequired", true, "error", exception.getMessage());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return Map.of("error", exception.getMessage());
        }
    }
}
