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
        // =====================================================

        Flight flight1 = createFlight(
                "FL001",
                "SAS",
                "Stockholm",
                "London",
                1
        );

        Flight flight2 = createFlight(
                "FL002",
                "Lufthansa",
                "Berlin",
                "Paris",
                2
        );

        Flight flight3 = createFlight(
                "FL003",
                "Ryanair",
                "Gothenburg",
                "Rome",
                3
        );

        Flight flight4 = createFlight(
                "FL004",
                "SAS",
                "Stockholm",
                "Copenhagen",
                4
        );

        Flight flight5 = createFlight(
                "FL005",
                "KLM",
                "Amsterdam",
                "Barcelona",
                5
        );

        Flight flight6 = createFlight(
                "FL006",
                "Lufthansa",
                "Frankfurt",
                "Madrid",
                6
        );

        Flight flight7 = createFlight(
                "FL007",
                "SAS",
                "Gothenburg",
                "Paris",
                7
        );

        Flight flight8 = createFlight(
                "FL008",
                "Ryanair",
                "Stockholm",
                "Berlin",
                8
        );

        Flight flight9 = createFlight(
                "FL009",
                "KLM",
                "Amsterdam",
                "Rome",
                9
        );

        Flight flight10 = createFlight(
                "FL010",
                "SAS",
                "Stockholm",
                "Amsterdam",
                10
        );

        flightRepository.saveAll(
                List.of(
                        flight1,
                        flight2,
                        flight3,
                        flight4,
                        flight5,
                        flight6,
                        flight7,
                        flight8,
                        flight9,
                        flight10
                )
        );

        // =====================================================
        // BOOKINGS
        // =====================================================

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

        Booking booking3 = createBooking(
                john,
                flight3,
                flight4,
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

        System.out.println("======================================");
        System.out.println("Sample data created successfully");
        System.out.println("Users: " + userRepository.count());
        System.out.println("Flights: " + flightRepository.count());
        System.out.println("Bookings: " + bookingRepository.count());
        System.out.println("======================================");
        System.out.println("Test login:");
        System.out.println("john@test.com / password123");
        System.out.println("jane@test.com / password123");
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
            int daysLater
    ) {

        LocalDateTime departure =
                LocalDateTime.now()
                        .plusDays(daysLater)
                        .withHour(10)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

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


    private String generateBookingReference() {

        return "FB-"
                +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}