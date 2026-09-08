package se.lexicon.flightbooking_api.assistant.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.assistant.exception.AssistantAuthenticationRequiredException;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Read-only assistant tools.
 *
 * Booking creation, seat selection and cancellation are intentionally handled by
 * the normal React booking components and the normal booking REST endpoints.
 * This keeps one booking implementation instead of maintaining an AI-specific one.
 */
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

    @Tool(description = "Search available flights between two trusted airport database IDs on an optional ISO date.")
    public Object searchAvailableFlights(
            @ToolParam(description = "Origin airport database ID") Long originAirportId,
            @ToolParam(description = "Destination airport database ID") Long destinationAirportId,
            @ToolParam(description = "Optional date in YYYY-MM-DD format", required = false) String departureDate
    ) {
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

    @Tool(description = "Resolve conversational city, airport, or IATA values and search flights on an ISO date. Use this when database airport IDs were not supplied by the React flight-search modal.")
    public Object searchFlightsByLocations(
            @ToolParam(description = "Origin city, airport name, or IATA code") String origin,
            @ToolParam(description = "Destination city, airport name, or IATA code") String destination,
            @ToolParam(description = "Departure date in YYYY-MM-DD format") String departureDate
    ) {
        var args = new AssistantToolDefinitions.SearchFlightsByLocations();
        args.origin = origin;
        args.destination = destination;
        args.departureDate = departureDate;

        return execute(() -> {
            var value = executor.searchFlightsByLocations(args);
            results.flights(value);
            return value;
        });
    }

    @Tool(description = "Get active or archived bookings owned by the authenticated user. Never request an email for authorization.")
    public Object getMyBookings(
            @ToolParam(description = "Whether archived bookings should be returned", required = false)
            boolean archived
    ) {
        var args = new AssistantToolDefinitions.GetMyBookings();
        args.archived = archived;

        return execute(() -> {
            var value = executor.getMyBookings(args);
            results.bookings(value);
            return value;
        });
    }

    private Object execute(Supplier<Object> operation) {
        try {
            return operation.get();
        } catch (AssistantAuthenticationRequiredException exception) {
            results.authenticationRequired();
            return Map.of(
                    "authenticationRequired", true,
                    "error", exception.getMessage()
            );
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return Map.of("error", exception.getMessage());
        }
    }
}
