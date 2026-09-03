package se.lexicon.flightbooking_api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.FlightSeat;
import se.lexicon.flightbooking_api.entity.Passenger;
import se.lexicon.flightbooking_api.entity.User;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.entity.enums.TripType;
import se.lexicon.flightbooking_api.entity.enums.UserRole;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.BookingRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;
import se.lexicon.flightbooking_api.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingControllerTest {

    private static final String TEST_EMAIL =
            "jane@test.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createBookingReturnsCreatedBooking() throws Exception {
        createUser();

        Airport origin = createAirport(
                "ARN",
                "Stockholm Arlanda Airport",
                "Stockholm",
                "Sweden"
        );

        Airport destination = createAirport(
                "LHR",
                "Heathrow Airport",
                "London",
                "United Kingdom"
        );

        Flight flight = createFlight(
                "SK001",
                origin,
                destination
        );

        FlightSeat seat = flight.getSeats().getFirst();

        mockMvc.perform(
                        post("/api/bookings")
                                .with(
                                        user(TEST_EMAIL)
                                                .roles("USER")
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "outboundFlightId": %d,
                                          "returnFlightId": null,
                                          "passengers": [
                                            {
                                              "firstName": "Jane",
                                              "lastName": "Doe",
                                              "passportNumber": "P123456",
                                              "email": "jane@test.com",
                                              "outboundSeatId": %d,
                                              "returnSeatId": null
                                            }
                                          ]
                                        }
                                        """.formatted(
                                        flight.getId(),
                                        seat.getId()
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id").isNumber()
                )
                .andExpect(
                        jsonPath("$.bookingReference").isString()
                )
                .andExpect(
                        jsonPath("$.status").value("CONFIRMED")
                )
                .andExpect(
                        jsonPath("$.outboundFlight.id")
                                .value(flight.getId())
                )
                .andExpect(
                        jsonPath("$.totalPrice")
                                .value(199.99)
                );
    }

    @Test
    void getMyBookingsReturnsOnlyAuthenticatedUsersBookings()
            throws Exception {

        User user = createUser();

        Airport origin = createAirport(
                "ARN",
                "Stockholm Arlanda Airport",
                "Stockholm",
                "Sweden"
        );

        Airport destination = createAirport(
                "LHR",
                "Heathrow Airport",
                "London",
                "United Kingdom"
        );

        Flight flight = createFlight(
                "SK002",
                origin,
                destination
        );

        createBooking(user, flight);

        mockMvc.perform(
                        get("/api/bookings/my")
                                .param("archived", "false")
                                .with(
                                        user(TEST_EMAIL)
                                                .roles("USER")
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(1)
                )
                .andExpect(
                        jsonPath("$[0].bookingReference")
                                .value("FB-12345")
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("CONFIRMED")
                );
    }

    @Test
    void getMyBookingsRequiresAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/bookings/my")
                                .param("archived", "false")
                )
                .andExpect(status().isUnauthorized());
    }

    private User createUser() {
        return userRepository.save(
                User.builder()
                        .email(TEST_EMAIL)
                        .password("encoded-password")
                        .role(UserRole.USER)
                        .build()
        );
    }

    private Airport createAirport(
            String code,
            String name,
            String city,
            String country
    ) {
        return airportRepository.save(
                Airport.builder()
                        .code(code)
                        .name(name)
                        .city(city)
                        .country(country)
                        .build()
        );
    }

    private Flight createFlight(
            String flightNumber,
            Airport origin,
            Airport destination
    ) {
        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .airline("SAS")
                .origin(origin)
                .destination(destination)
                .departureTime(
                        LocalDateTime.now().plusDays(5)
                )
                .arrivalTime(
                        LocalDateTime.now()
                                .plusDays(5)
                                .plusHours(2)
                )
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightSeat seat = FlightSeat.builder()
                .seatNumber("12A")
                .seatClass(SeatClass.ECONOMY)
                .price(new BigDecimal("199.99"))
                .build();

        flight.addSeat(seat);

        return flightRepository.saveAndFlush(flight);
    }

    private Booking createBooking(
            User user,
            Flight flight
    ) {
        Passenger passenger = Passenger.builder()
                .firstName("Jane")
                .lastName("Doe")
                .passportNumber("P123456")
                .email(TEST_EMAIL)
                .build();

        Booking booking = Booking.builder()
                .user(user)
                .bookingReference("FB-12345")
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .tripType(TripType.ONE_WAY)
                .outboundFlight(flight)
                .totalPrice(new BigDecimal("199.99"))
                .build();

        booking.addPassenger(passenger);

        return bookingRepository.saveAndFlush(booking);
    }
}