package se.lexicon.flightbooking_api.assistant.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import se.lexicon.flightbooking_api.service.AirportService;
import se.lexicon.flightbooking_api.service.FlightService;
import se.lexicon.flightbooking_api.service.FlightSeatService;
import se.lexicon.flightbooking_api.service.BookingService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.core.context.SecurityContextHolder;
import se.lexicon.flightbooking_api.assistant.exception.AssistantAuthenticationRequiredException;

class AssistantToolExecutorTest {

    private AirportService airportService;
    private FlightService flightService;
    private AssistantToolExecutor toolExecutor;
    private FlightSeatService flightSeatService;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        airportService = mock(AirportService.class);
        flightService = mock(FlightService.class);
        flightSeatService = mock(FlightSeatService.class);
        bookingService = mock(BookingService.class);

        toolExecutor = new AssistantToolExecutor(
                airportService,
                flightService,
                flightSeatService,
                bookingService
        );
    }

    @Test
    void searchesAirportsThroughAirportService() {
        AirportResponseDto airport =
                new AirportResponseDto(
                        1L,
                        "GOT",
                        "Göteborg Landvetter Airport",
                        "Gothenburg",
                        "Sweden"
                );

        when(airportService.search("Gothenburg", 10))
                .thenReturn(List.of(airport));

        AssistantToolDefinitions.SearchAirports arguments =
                new AssistantToolDefinitions.SearchAirports();

        arguments.query = "  Gothenburg  ";

        List<AirportResponseDto> result =
                toolExecutor.searchAirports(arguments);

        assertThat(result).containsExactly(airport);

        verify(airportService)
                .search("Gothenburg", 10);
    }

    @Test
    void rejectsMissingAirportQuery() {
        AssistantToolDefinitions.SearchAirports arguments =
                new AssistantToolDefinitions.SearchAirports();

        assertThatThrownBy(
                () -> toolExecutor.searchAirports(arguments)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Airport search query is required"
                );
    }

    @Test
    void rejectsQueryShorterThanTwoCharacters() {
        AssistantToolDefinitions.SearchAirports arguments =
                new AssistantToolDefinitions.SearchAirports();

        arguments.query = "G";

        assertThatThrownBy(
                () -> toolExecutor.searchAirports(arguments)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Airport search query must contain "
                                + "at least 2 characters"
                );
    }

    @Test
    void searchesAvailableFlightsAndFiltersByDate() {
        AirportResponseDto origin =
                new AirportResponseDto(
                        2L,
                        "GOT",
                        "Goteborg Landvetter Airport",
                        "Gothenburg",
                        "Sweden"
                );

        AirportResponseDto destination =
                new AirportResponseDto(
                        5L,
                        "CDG",
                        "Charles de Gaulle Airport",
                        "Paris",
                        "France"
                );

        FlightDto matchingFlight =
                new FlightDto(
                        10L,
                        "SK123",
                        "SAS",
                        origin,
                        destination,
                        LocalDateTime.of(
                                2026, 9, 11, 10, 0
                        ),
                        LocalDateTime.of(
                                2026, 9, 11, 12, 30
                        ),
                        FlightStatus.SCHEDULED,
                        new BigDecimal("1299.00")
                );

        FlightDto differentDate =
                new FlightDto(
                        11L,
                        "SK125",
                        "SAS",
                        origin,
                        destination,
                        LocalDateTime.of(
                                2026, 9, 12, 10, 0
                        ),
                        LocalDateTime.of(
                                2026, 9, 12, 12, 30
                        ),
                        FlightStatus.SCHEDULED,
                        new BigDecimal("1399.00")
                );

        when(flightService.getAvailableFlights(2L, 5L))
                .thenReturn(
                        List.of(
                                matchingFlight,
                                differentDate
                        )
                );

        AssistantToolDefinitions.SearchAvailableFlights arguments =
                new AssistantToolDefinitions.SearchAvailableFlights();

        arguments.originAirportId = 2L;
        arguments.destinationAirportId = 5L;
        arguments.departureDate = "2026-09-11";

        List<FlightDto> result =
                toolExecutor.searchAvailableFlights(arguments);

        assertThat(result).containsExactly(matchingFlight);

        verify(flightService)
                .getAvailableFlights(2L, 5L);
    }

    @Test
    void rejectsFlightSearchWithoutDestination() {
        AssistantToolDefinitions.SearchAvailableFlights arguments =
                new AssistantToolDefinitions.SearchAvailableFlights();

        arguments.originAirportId = 2L;

        assertThatThrownBy(
                () -> toolExecutor.searchAvailableFlights(arguments)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Destination airport ID is required"
                );
    }

    @Test
    void anonymousUserCannotReadPersonalBookings() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> toolExecutor.getMyBookings(
                new AssistantToolDefinitions.GetMyBookings()))
                .isInstanceOf(AssistantAuthenticationRequiredException.class)
                .hasMessage("Please sign in to access or manage bookings");
    }
}
