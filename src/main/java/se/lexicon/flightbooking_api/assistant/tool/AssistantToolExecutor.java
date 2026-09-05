package se.lexicon.flightbooking_api.assistant.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import se.lexicon.flightbooking_api.assistant.action.PendingActionProposal;
import se.lexicon.flightbooking_api.assistant.exception.AssistantAuthenticationRequiredException;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.dto.passenger.PassengerRequestDto;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.service.AirportService;
import se.lexicon.flightbooking_api.service.BookingService;
import se.lexicon.flightbooking_api.service.FlightSeatService;

import java.util.List;

import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.service.FlightService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class AssistantToolExecutor {

    private static final int AIRPORT_RESULT_LIMIT = 10;

    private final AirportService airportService;
    private final FlightService flightService;
    private final FlightSeatService flightSeatService;
    private final BookingService bookingService;

    public List<AirportResponseDto> searchAirports(
            AssistantToolDefinitions.SearchAirports arguments
    ) {
        if (arguments == null
                || arguments.query == null
                || arguments.query.isBlank()) {
            throw new IllegalArgumentException(
                    "Airport search query is required"
            );
        }

        String normalizedQuery = arguments.query.trim();

        if (normalizedQuery.length() < 2) {
            throw new IllegalArgumentException(
                    "Airport search query must contain at least 2 characters"
            );
        }

        return airportService.search(
                normalizedQuery,
                AIRPORT_RESULT_LIMIT
        );
    }

    public List<FlightDto> searchAvailableFlights(
            AssistantToolDefinitions.SearchAvailableFlights arguments
    ) {
        if (arguments == null) {
            throw new IllegalArgumentException(
                    "Flight search arguments are required"
            );
        }

        if (arguments.originAirportId == null) {
            throw new IllegalArgumentException(
                    "Origin airport ID is required"
            );
        }

        if (arguments.destinationAirportId == null) {
            throw new IllegalArgumentException(
                    "Destination airport ID is required"
            );
        }

        if (arguments.originAirportId.equals(
                arguments.destinationAirportId
        )) {
            throw new IllegalArgumentException(
                    "Origin and destination must be different"
            );
        }

        List<FlightDto> flights =
                flightService.getAvailableFlights(
                        arguments.originAirportId,
                        arguments.destinationAirportId
                );

        if (arguments.departureDate == null
                || arguments.departureDate.isBlank()) {
            return flights;
        }

        LocalDate departureDate;

        try {
            departureDate = LocalDate.parse(
                    arguments.departureDate.trim()
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Departure date must use YYYY-MM-DD format"
            );
        }

        return flights.stream()
                .filter(flight ->
                        flight.departureTime() != null
                                && flight.departureTime()
                                .toLocalDate()
                                .equals(departureDate)
                )
                .toList();
    }

    public List<FlightSeatDto> getAvailableSeats(
            AssistantToolDefinitions.GetAvailableSeats arguments
    ) {
        if (arguments == null || arguments.flightId == null) {
            throw new IllegalArgumentException("Flight ID is required");
        }

        if (arguments.seatClass == null || arguments.seatClass.isBlank()) {
            return flightSeatService.getAvailableSeats(arguments.flightId);
        }

        try {
            SeatClass seatClass = SeatClass.valueOf(
                    arguments.seatClass.trim().toUpperCase()
            );
            return flightSeatService.getAvailableSeatsByClass(
                    arguments.flightId,
                    seatClass
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Seat class must be ECONOMY, PREMIUM_ECONOMY, BUSINESS or FIRST_CLASS"
            );
        }
    }

    public List<BookingResponseDto> getMyBookings(
            AssistantToolDefinitions.GetMyBookings arguments
    ) {
        requireAuthenticatedUser();
        boolean archived = arguments != null && arguments.archived;
        return bookingService.getMyBookings(archived);
    }

    public PendingActionProposal prepareCreateBooking(
            AssistantToolDefinitions.CreateBooking arguments
    ) {
        requireAuthenticatedUser();
        if (arguments == null || arguments.outboundFlightId == null) {
            throw new IllegalArgumentException(
                    "Outbound flight ID is required"
            );
        }
        if (arguments.passengers == null || arguments.passengers.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one passenger is required"
            );
        }

        List<PassengerRequestDto> passengers = arguments.passengers.stream()
                .map(this::toPassengerRequest)
                .toList();

        BookingRequestDto request = new BookingRequestDto(
                arguments.outboundFlightId,
                arguments.returnFlightId,
                passengers
        );

        String description = "Create a booking for "
                + passengers.size()
                + " passenger(s) on outbound flight "
                + arguments.outboundFlightId
                + (arguments.returnFlightId == null
                ? ""
                : " and return flight " + arguments.returnFlightId);

        return PendingActionProposal.createBooking(description, request);
    }

    public PendingActionProposal prepareCancelBooking(
            AssistantToolDefinitions.CancelBooking arguments
    ) {
        requireAuthenticatedUser();
        if (arguments == null || arguments.bookingId == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        boolean owned = bookingService.getMyBookings().stream()
                .anyMatch(booking -> booking.id().equals(arguments.bookingId));
        if (!owned) {
            throw new IllegalArgumentException(
                    "Booking was not found for the authenticated user"
            );
        }

        return PendingActionProposal.cancelBooking(
                "Cancel booking " + arguments.bookingId,
                arguments.bookingId
        );
    }

    public BookingResponseDto createBooking(BookingRequestDto request) {
        requireAuthenticatedUser();
        return bookingService.createBooking(request);
    }

    public BookingResponseDto cancelBooking(Long bookingId) {
        requireAuthenticatedUser();
        bookingService.cancelBooking(bookingId);
        return bookingService.getMyBookings().stream()
                .filter(booking -> booking.id().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cancelled booking could not be loaded"
                ));
    }

    private PassengerRequestDto toPassengerRequest(
            AssistantToolDefinitions.PassengerInput passenger
    ) {
        if (passenger == null
                || isBlank(passenger.firstName)
                || isBlank(passenger.lastName)
                || isBlank(passenger.passportNumber)
                || isBlank(passenger.email)
                || passenger.outboundSeatId == null) {
            throw new IllegalArgumentException(
                    "Each passenger requires first name, last name, passport "
                            + "number, email and outbound seat ID"
            );
        }
        return new PassengerRequestDto(
                passenger.firstName.trim(),
                passenger.lastName.trim(),
                passenger.passportNumber.trim(),
                passenger.email.trim(),
                passenger.outboundSeatId,
                passenger.returnSeatId
        );
    }

    private void requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AssistantAuthenticationRequiredException(
                    "Please sign in to access or manage bookings"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
