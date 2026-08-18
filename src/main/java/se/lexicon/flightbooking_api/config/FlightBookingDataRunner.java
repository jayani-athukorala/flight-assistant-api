package se.lexicon.flightbooking_api.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.entity.*;
import se.lexicon.flightbooking_api.entity.enums.*;
import se.lexicon.flightbooking_api.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class FlightBookingDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {

        if (userRepository.findByEmail("john@test.com").isPresent()) {
            System.out.println("Sample data already exists. Skipping creation.");
            return;
        }

        System.out.println("Creating sample flight booking data...");

        // =====================================================
        // USERS
        // =====================================================

        User john = createUser(
                "john@test.com",
                "password123",
                UserRole.USER
        );

        User jane = createUser(
                "jane@test.com",
                "password123",
                UserRole.USER
        );

        userRepository.saveAll(
                List.of(john, jane)
        );

        // =====================================================
        // FLIGHTS
        //
        // IMPORTANT:
        // The same flight number may appear on different dates.
        //
        // Example:
        // SK1234 - Gothenburg -> Paris - Aug 10
        // SK1234 - Gothenburg -> Paris - Aug 13
        //
        // Return flights are separate Flight entities:
        //
        // SK1235 - Paris -> Gothenburg - Aug 15
        // =====================================================

        List<Flight> flights = new ArrayList<>();

        // -----------------------------------------------------
        // AUGUST 9, 2026 - 5 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "SK1001",
                "SAS",
                "Stockholm",
                "London",
                LocalDateTime.of(2026, 8, 9, 7, 30)
        ));

        flights.add(createFlight(
                "LH2001",
                "Lufthansa",
                "Berlin",
                "Paris",
                LocalDateTime.of(2026, 8, 9, 9, 15)
        ));

        flights.add(createFlight(
                "FR3001",
                "Ryanair",
                "Gothenburg",
                "Rome",
                LocalDateTime.of(2026, 8, 9, 11, 45)
        ));

        flights.add(createFlight(
                "SK1002",
                "SAS",
                "Stockholm",
                "Copenhagen",
                LocalDateTime.of(2026, 8, 9, 14, 30)
        ));

        flights.add(createFlight(
                "KL4001",
                "KLM",
                "Amsterdam",
                "Barcelona",
                LocalDateTime.of(2026, 8, 9, 18, 00)
        ));

        // -----------------------------------------------------
        // AUGUST 10, 2026 - 7 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "LH2002",
                "Lufthansa",
                "Frankfurt",
                "Madrid",
                LocalDateTime.of(2026, 8, 10, 6, 45)
        ));

        // GOTHENBURG -> PARIS
        flights.add(createFlight(
                "SK1234",
                "SAS",
                "Gothenburg",
                "Paris",
                LocalDateTime.of(2026, 8, 10, 8, 30)
        ));

        flights.add(createFlight(
                "FR3002",
                "Ryanair",
                "Stockholm",
                "Berlin",
                LocalDateTime.of(2026, 8, 10, 10, 15)
        ));

        flights.add(createFlight(
                "KL4002",
                "KLM",
                "Amsterdam",
                "Rome",
                LocalDateTime.of(2026, 8, 10, 13, 00)
        ));

        flights.add(createFlight(
                "SK1003",
                "SAS",
                "Stockholm",
                "Amsterdam",
                LocalDateTime.of(2026, 8, 10, 15, 45)
        ));

        flights.add(createFlight(
                "LH2003",
                "Lufthansa",
                "Berlin",
                "Vienna",
                LocalDateTime.of(2026, 8, 10, 19, 15)
        ));

        // RETURN FLIGHT: PARIS -> GOTHENBURG
        flights.add(createFlight(
                "SK1235",
                "SAS",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 10, 20, 30)
        ));

        // -----------------------------------------------------
        // AUGUST 11, 2026 - 6 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "SK1101",
                "SAS",
                "Gothenburg",
                "London",
                LocalDateTime.of(2026, 8, 11, 8, 00)
        ));

        flights.add(createFlight(
                "KL4101",
                "KLM",
                "Amsterdam",
                "Paris",
                LocalDateTime.of(2026, 8, 11, 11, 30)
        ));

        flights.add(createFlight(
                "LH2101",
                "Lufthansa",
                "Frankfurt",
                "Rome",
                LocalDateTime.of(2026, 8, 11, 14, 15)
        ));

        flights.add(createFlight(
                "FR3101",
                "Ryanair",
                "Stockholm",
                "Barcelona",
                LocalDateTime.of(2026, 8, 11, 18, 45)
        ));

        // Same flight number as Aug 10.
        // This demonstrates that a flight number can repeat
        // on another date.
        flights.add(createFlight(
                "SK1234",
                "SAS",
                "Gothenburg",
                "Paris",
                LocalDateTime.of(2026, 8, 11, 9, 45)
        ));

        flights.add(createFlight(
                "SK1235",
                "SAS",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 11, 17, 30)
        ));

        // -----------------------------------------------------
        // AUGUST 12, 2026 - 8 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "SK1201",
                "SAS",
                "Stockholm",
                "Berlin",
                LocalDateTime.of(2026, 8, 12, 6, 30)
        ));

        flights.add(createFlight(
                "LH2201",
                "Lufthansa",
                "Berlin",
                "London",
                LocalDateTime.of(2026, 8, 12, 8, 15)
        ));

        flights.add(createFlight(
                "FR3201",
                "Ryanair",
                "Gothenburg",
                "Barcelona",
                LocalDateTime.of(2026, 8, 12, 10, 00)
        ));

        flights.add(createFlight(
                "KL4201",
                "KLM",
                "Amsterdam",
                "Madrid",
                LocalDateTime.of(2026, 8, 12, 12, 30)
        ));

        flights.add(createFlight(
                "SK1202",
                "SAS",
                "Stockholm",
                "Paris",
                LocalDateTime.of(2026, 8, 12, 15, 00)
        ));

        flights.add(createFlight(
                "LH2202",
                "Lufthansa",
                "Frankfurt",
                "Copenhagen",
                LocalDateTime.of(2026, 8, 12, 17, 30)
        ));

        flights.add(createFlight(
                "FR3202",
                "Ryanair",
                "Gothenburg",
                "Rome",
                LocalDateTime.of(2026, 8, 12, 20, 15)
        ));

        // RETURN FLIGHT
        flights.add(createFlight(
                "SK1235",
                "SAS",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 12, 21, 00)
        ));

        // -----------------------------------------------------
        // AUGUST 13, 2026 - 7 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "SK1301",
                "SAS",
                "Stockholm",
                "Oslo",
                LocalDateTime.of(2026, 8, 13, 7, 45)
        ));

        flights.add(createFlight(
                "LH2301",
                "Lufthansa",
                "Berlin",
                "Madrid",
                LocalDateTime.of(2026, 8, 13, 10, 30)
        ));

        flights.add(createFlight(
                "KL4301",
                "KLM",
                "Amsterdam",
                "London",
                LocalDateTime.of(2026, 8, 13, 13, 15)
        ));

        // GOTHENBURG -> PARIS
        // Same flight number as Aug 10 and Aug 11.
        flights.add(createFlight(
                "SK1234",
                "SAS",
                "Gothenburg",
                "Paris",
                LocalDateTime.of(2026, 8, 13, 16, 00)
        ));

        flights.add(createFlight(
                "SK1302",
                "SAS",
                "Stockholm",
                "Rome",
                LocalDateTime.of(2026, 8, 13, 19, 30)
        ));

        // PARIS -> GOTHENBURG
        flights.add(createFlight(
                "SK1235",
                "SAS",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 13, 20, 30)
        ));

        flights.add(createFlight(
                "FR3301",
                "Ryanair",
                "Gothenburg",
                "Milan",
                LocalDateTime.of(2026, 8, 13, 21, 15)
        ));

        // -----------------------------------------------------
        // AUGUST 14, 2026 - 7 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "LH2401",
                "Lufthansa",
                "Frankfurt",
                "Paris",
                LocalDateTime.of(2026, 8, 14, 6, 45)
        ));

        flights.add(createFlight(
                "SK1401",
                "SAS",
                "Gothenburg",
                "Amsterdam",
                LocalDateTime.of(2026, 8, 14, 8, 30)
        ));

        flights.add(createFlight(
                "FR3401",
                "Ryanair",
                "Stockholm",
                "Madrid",
                LocalDateTime.of(2026, 8, 14, 11, 00)
        ));

        flights.add(createFlight(
                "KL4401",
                "KLM",
                "Amsterdam",
                "Barcelona",
                LocalDateTime.of(2026, 8, 14, 13, 45)
        ));

        flights.add(createFlight(
                "SK1402",
                "SAS",
                "Stockholm",
                "Copenhagen",
                LocalDateTime.of(2026, 8, 14, 16, 30)
        ));

        flights.add(createFlight(
                "LH2402",
                "Lufthansa",
                "Berlin",
                "Vienna",
                LocalDateTime.of(2026, 8, 14, 19, 00)
        ));

        // RETURN FLIGHT
        flights.add(createFlight(
                "SK1235",
                "SAS",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 14, 21, 00)
        ));

        // -----------------------------------------------------
        // AUGUST 15, 2026 - 7 FLIGHTS
        // -----------------------------------------------------

        flights.add(createFlight(
                "SK1501",
                "SAS",
                "Gothenburg",
                "London",
                LocalDateTime.of(2026, 8, 15, 7, 30)
        ));

        flights.add(createFlight(
                "FR3501",
                "Ryanair",
                "Stockholm",
                "Berlin",
                LocalDateTime.of(2026, 8, 15, 10, 15)
        ));

        flights.add(createFlight(
                "KL4501",
                "KLM",
                "Amsterdam",
                "Rome",
                LocalDateTime.of(2026, 8, 15, 14, 00)
        ));

        flights.add(createFlight(
                "LH2501",
                "Lufthansa",
                "Frankfurt",
                "Madrid",
                LocalDateTime.of(2026, 8, 15, 18, 30)
        ));

        // GOTHENBURG -> PARIS
        flights.add(createFlight(
                "SK1234",
                "SAS",
                "Gothenburg",
                "Paris",
                LocalDateTime.of(2026, 8, 15, 9, 00)
        ));

        // PARIS -> GOTHENBURG
        flights.add(createFlight(
                "SK1235",
                "SAS",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 15, 17, 00)
        ));

        flights.add(createFlight(
                "FR3502",
                "Ryanair",
                "Gothenburg",
                "Rome",
                LocalDateTime.of(2026, 8, 15, 20, 30)
        ));

        // =====================================================
        // SAVE ALL FLIGHTS
        // =====================================================

        flightRepository.saveAll(flights);

        // =====================================================
        // BOOKINGS
        // =====================================================

        // -----------------------------------------------------
        // ONE-WAY BOOKING
        // Stockholm -> London
        // -----------------------------------------------------

        Flight flight1 = findFlight(
                flights,
                "SK1001",
                "Stockholm",
                "London",
                LocalDateTime.of(2026, 8, 9, 7, 30)
        );

        Booking booking1 = createBooking(
                john,
                flight1,
                null,
                TripType.ONE_WAY,
                "John",
                "Doe",
                "PASS12345",
                "john@test.com",
                599.99
        );

        // -----------------------------------------------------
        // ONE-WAY BOOKING
        // Berlin -> Paris
        // -----------------------------------------------------

        Flight flight2 = findFlight(
                flights,
                "LH2001",
                "Berlin",
                "Paris",
                LocalDateTime.of(2026, 8, 9, 9, 15)
        );

        Booking booking2 = createBooking(
                jane,
                flight2,
                null,
                TripType.ONE_WAY,
                "Jane",
                "Smith",
                "PASS67890",
                "jane@test.com",
                299.99
        );

        // -----------------------------------------------------
        // ROUND-TRIP BOOKING
        //
        // Gothenburg -> Paris
        // Paris -> Gothenburg
        //
        // Outbound: August 10
        // Return:   August 15
        // -----------------------------------------------------

        Flight gothenburgToParis = findFlight(
                flights,
                "SK1234",
                "Gothenburg",
                "Paris",
                LocalDateTime.of(2026, 8, 10, 8, 30)
        );

        Flight parisToGothenburg = findFlight(
                flights,
                "SK1235",
                "Paris",
                "Gothenburg",
                LocalDateTime.of(2026, 8, 15, 17, 00)
        );

        Booking booking3 = createBooking(
                john,
                gothenburgToParis,
                parisToGothenburg,
                TripType.ROUND_TRIP,
                "John",
                "Doe",
                "PASS54321",
                "john@test.com",
                899.99
        );

        bookingRepository.saveAll(
                List.of(
                        booking1,
                        booking2,
                        booking3
                )
        );

        // =====================================================
        // SUMMARY
        // =====================================================

        System.out.println("======================================");
        System.out.println("Sample data created successfully");
        System.out.println("Users: " + userRepository.count());
        System.out.println("Flights: " + flightRepository.count());
        System.out.println("Bookings: " + bookingRepository.count());
        System.out.println("======================================");

        System.out.println("Flights created for:");

        System.out.println("August 09, 2026: 5 flights");
        System.out.println("August 10, 2026: 7 flights");
        System.out.println("August 11, 2026: 6 flights");
        System.out.println("August 12, 2026: 8 flights");
        System.out.println("August 13, 2026: 7 flights");
        System.out.println("August 14, 2026: 7 flights");
        System.out.println("August 15, 2026: 7 flights");

        System.out.println("Total flights: " + flights.size());

        System.out.println("======================================");
        System.out.println("Example repeated flight numbers:");
        System.out.println("SK1234 - Gothenburg -> Paris");
        System.out.println("  Aug 10, 2026 08:30");
        System.out.println("  Aug 11, 2026 09:45");
        System.out.println("  Aug 13, 2026 16:00");
        System.out.println("  Aug 15, 2026 09:00");

        System.out.println("--------------------------------------");

        System.out.println("SK1235 - Paris -> Gothenburg");
        System.out.println("  Aug 10, 2026 20:30");
        System.out.println("  Aug 11, 2026 17:30");
        System.out.println("  Aug 12, 2026 21:00");
        System.out.println("  Aug 13, 2026 20:30");
        System.out.println("  Aug 14, 2026 21:00");
        System.out.println("  Aug 15, 2026 17:00");

        System.out.println("======================================");
        System.out.println("Round-trip sample booking:");
        System.out.println("Gothenburg -> Paris");
        System.out.println("SK1234 - August 10, 2026 08:30");
        System.out.println();
        System.out.println("Paris -> Gothenburg");
        System.out.println("SK1235 - August 15, 2026 17:00");

        System.out.println("======================================");
        System.out.println("Test login:");
        System.out.println("john@test.com / password123");
        System.out.println("jane@test.com / password123");
        System.out.println("======================================");
    }

    // =========================================================
    // FIND FLIGHT
    //
    // We deliberately search using flight number AND date/time
    // AND route because flight number alone is not unique.
    // =========================================================

    private Flight findFlight(
            List<Flight> flights,
            String flightNumber,
            String origin,
            String destination,
            LocalDateTime departure
    ) {

        return flights.stream()
                .filter(flight ->
                        flight.getFlightNumber().equals(flightNumber)
                                && flight.getOrigin().equals(origin)
                                && flight.getDestination().equals(destination)
                                && flight.getDepartureTime().equals(departure)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Flight not found: "
                                        + flightNumber
                                        + " "
                                        + origin
                                        + " -> "
                                        + destination
                                        + " at "
                                        + departure
                        )
                );
    }

    // =========================================================
    // USER
    // =========================================================

    private User createUser(
            String email,
            String password,
            UserRole role
    ) {

        return User.builder()
                .email(email)
                .password(
                        passwordEncoder.encode(password)
                )
                .role(role)
                .build();
    }

    // =========================================================
    // FLIGHT
    // =========================================================

    private Flight createFlight(
            String flightNumber,
            String airline,
            String origin,
            String destination,
            LocalDateTime departure
    ) {

        Flight flight =
                Flight.builder()
                        .flightNumber(flightNumber)
                        .airline(airline)
                        .origin(origin)
                        .destination(destination)
                        .departureTime(departure)
                        .arrivalTime(departure.plusHours(3))
                        .status(FlightStatus.AVAILABLE)
                        .build();

        createSeats(flight);

        return flight;
    }

    // =========================================================
    // SEATS
    // =========================================================

    private void createSeats(Flight flight) {

        // 5 Economy
        for (int i = 1; i <= 5; i++) {

            addSeat(
                    flight,
                    "E" + i,
                    SeatClass.ECONOMY,
                    199.99
            );
        }

        // 2 Premium Economy
        for (int i = 1; i <= 2; i++) {

            addSeat(
                    flight,
                    "PE" + i,
                    SeatClass.PREMIUM_ECONOMY,
                    299.99
            );
        }

        // 2 Business
        for (int i = 1; i <= 2; i++) {

            addSeat(
                    flight,
                    "B" + i,
                    SeatClass.BUSINESS,
                    599.99
            );
        }

        // 1 First Class
        addSeat(
                flight,
                "F1",
                SeatClass.FIRST_CLASS,
                999.99
        );
    }

    private void addSeat(
            Flight flight,
            String seatNumber,
            SeatClass seatClass,
            double price
    ) {

        FlightSeat seat =
                FlightSeat.builder()
                        .seatNumber(seatNumber)
                        .seatClass(seatClass)
                        .price(price)
                        .flight(flight)
                        .build();

        flight.getSeats().add(seat);
    }

    // =========================================================
    // BOOKING
    // =========================================================

    private Booking createBooking(
            User user,
            Flight outboundFlight,
            Flight returnFlight,
            TripType tripType,
            String firstName,
            String lastName,
            String passportNumber,
            String email,
            double totalPrice
    ) {

        Passenger passenger =
                Passenger.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .passportNumber(passportNumber)
                        .email(email)
                        .build();

        Booking booking =
                Booking.builder()
                        .user(user)
                        .bookingReference(
                                generateBookingReference()
                        )
                        .bookingDate(
                                LocalDateTime.now()
                        )
                        .status(
                                BookingStatus.CONFIRMED
                        )
                        .tripType(tripType)
                        .outboundFlight(outboundFlight)
                        .returnFlight(returnFlight)
                        .totalPrice(totalPrice)
                        .build();

        booking.addPassenger(passenger);

        return booking;
    }

    // =========================================================
    // BOOKING REFERENCE
    // =========================================================

    private String generateBookingReference() {

        return "FB-"
                +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}
