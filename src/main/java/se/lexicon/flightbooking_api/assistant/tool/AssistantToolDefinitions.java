package se.lexicon.flightbooking_api.assistant.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

public final class AssistantToolDefinitions {

    private AssistantToolDefinitions() {
    }

    @JsonTypeName("search_airports")
    @JsonClassDescription(
            "Searches airports using a city, airport name, "
                    + "country or IATA airport code."
    )
    public static class SearchAirports {

        @JsonPropertyDescription(
                "Search text such as Gothenburg, Paris, GOT or CDG."
        )
        public String query;
    }

    @JsonTypeName("search_available_flights")
    @JsonClassDescription(
            "Searches scheduled flights between two airports. "
                    + "Use search_airports first to resolve airport IDs."
    )
    public static class SearchAvailableFlights {

        @JsonPropertyDescription(
                "Database ID of the origin airport."
        )
        public Long originAirportId;

        @JsonPropertyDescription(
                "Database ID of the destination airport."
        )
        public Long destinationAirportId;

        @JsonPropertyDescription(
                "Optional departure date in ISO-8601 format YYYY-MM-DD."
        )
        public String departureDate;
    }

    @JsonTypeName("get_available_seats")
    @JsonClassDescription(
            "Returns currently available seats for a flight. "
                    + "This operation is public."
    )
    public static class GetAvailableSeats {
        @JsonPropertyDescription("Database ID of the selected flight.")
        public Long flightId;

        @JsonPropertyDescription(
                "Optional seat class: ECONOMY, BUSINESS or FIRST."
        )
        public String seatClass;
    }

    @JsonTypeName("get_my_bookings")
    @JsonClassDescription(
            "Returns bookings owned by the authenticated user. "
                    + "Never ask the user for an email address."
    )
    public static class GetMyBookings {
        @JsonPropertyDescription(
                "True to return archived bookings; otherwise false."
        )
        public boolean archived;
    }

    public static class PassengerInput {
        @JsonPropertyDescription("Passenger first name.")
        public String firstName;

        @JsonPropertyDescription("Passenger last name.")
        public String lastName;

        @JsonPropertyDescription("Passenger passport number.")
        public String passportNumber;

        @JsonPropertyDescription("Passenger contact email. Not used for authorization.")
        public String email;

        @JsonPropertyDescription("Selected outbound flight-seat ID.")
        public Long outboundSeatId;

        @JsonPropertyDescription(
                "Selected return flight-seat ID, or null for a one-way trip."
        )
        public Long returnSeatId;
    }

    @JsonTypeName("create_booking")
    @JsonClassDescription(
            "Prepares a booking for explicit user confirmation. "
                    + "Calling this tool must never create the booking immediately."
    )
    public static class CreateBooking {
        @JsonPropertyDescription("Selected outbound flight ID.")
        public Long outboundFlightId;

        @JsonPropertyDescription(
                "Selected return flight ID, or null for a one-way trip."
        )
        public Long returnFlightId;

        @JsonPropertyDescription(
                "Passengers and their selected seats."
        )
        public List<PassengerInput> passengers;
    }

    @JsonTypeName("cancel_booking")
    @JsonClassDescription(
            "Prepares cancellation of a booking owned by the authenticated "
                    + "user for explicit confirmation. It must not cancel immediately."
    )
    public static class CancelBooking {
        @JsonPropertyDescription("ID of the booking to cancel.")
        public Long bookingId;
    }
}
