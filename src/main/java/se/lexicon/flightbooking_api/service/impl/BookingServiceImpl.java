package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.dto.passenger.PassengerRequestDto;
import se.lexicon.flightbooking_api.entity.*;
import se.lexicon.flightbooking_api.entity.enums.*;
import se.lexicon.flightbooking_api.exception.*;

import se.lexicon.flightbooking_api.mapper.BookingMapper;
import se.lexicon.flightbooking_api.mapper.PassengerMapper;

import se.lexicon.flightbooking_api.repository.*;

import se.lexicon.flightbooking_api.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final PassengerMapper passengerMapper;

    private String getAuthenticatedEmail() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        return authentication.getName();
    }


    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {

        // ==================================================
        // AUTHENTICATED USER
        // ==================================================

        String email = getAuthenticatedEmail();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + email
                        )
                );


        // ==================================================
        // OUTBOUND FLIGHT
        // ==================================================

        Flight outboundFlight =
                flightRepository.findById(request.outboundFlightId())
                        .orElseThrow(() ->
                                new FlightNotFoundException(
                                        request.outboundFlightId()
                                )
                        );

        validateFlightCanBeBooked(outboundFlight);


        // ==================================================
        // RETURN FLIGHT
        // ==================================================

        Flight returnFlight = null;
        TripType tripType = request.returnFlightId() == null
                ? TripType.ONE_WAY
                : TripType.ROUND_TRIP;

        if (request.returnFlightId() != null) {
            returnFlight =
                    flightRepository.findById(
                            request.returnFlightId()
                    ).orElseThrow(() ->
                            new FlightNotFoundException(
                                    request.returnFlightId()
                            )
                    );

            validateFlightCanBeBooked(returnFlight);
            validateReturnFlight(outboundFlight, returnFlight);
        }


        // ==================================================
        // CREATE BOOKING
        // ==================================================

        Booking booking = Booking.builder()
                .user(user)
                .bookingReference(generateBookingReference())
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .tripType(tripType)
                .outboundFlight(outboundFlight)
                .returnFlight(returnFlight)
                .totalPrice(BigDecimal.ZERO)
                .build();


        BigDecimal totalPrice = BigDecimal.ZERO;
        Set<Long> selectedSeatIds = new HashSet<>();


        // ==================================================
        // PASSENGERS + SEATS
        // ==================================================

        for (PassengerRequestDto passengerDto :
                request.passengers()) {

            // ----------------------------------------------
            // PASSENGER
            // ----------------------------------------------

            Passenger passenger =
                    createPassenger(passengerDto);

            booking.addPassenger(passenger);


            // ----------------------------------------------
            // OUTBOUND SEAT
            // ----------------------------------------------

            if (passengerDto.outboundSeatId() == null) {

                throw new IllegalArgumentException(
                        "Outbound seat is required"
                );
            }

            ensureSeatWasNotSelectedTwice(
                    selectedSeatIds,
                    passengerDto.outboundSeatId()
            );

            FlightSeat outboundSeat =
                    flightSeatRepository
                            .findById(
                                    passengerDto.outboundSeatId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Outbound seat not found: "
                                                    + passengerDto.outboundSeatId()
                                    )
                            );


            validateSeat(
                    outboundSeat,
                    outboundFlight
            );

            bookingSeatMustBeAvailable(outboundSeat);


            booking.addSeat(
                    outboundSeat,
                    passenger
            );


            totalPrice =
                    totalPrice.add(
                            outboundSeat.getPrice()
                    );


            // ----------------------------------------------
            // RETURN SEAT
            // ----------------------------------------------

            if (returnFlight != null) {

                if (passengerDto.returnSeatId() == null) {

                    throw new InvalidTripException(
                            "Return seat is required for a round trip"
                    );
                }

                ensureSeatWasNotSelectedTwice(
                        selectedSeatIds,
                        passengerDto.returnSeatId()
                );


                FlightSeat returnSeat =
                        flightSeatRepository
                                .findById(
                                        passengerDto.returnSeatId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Return seat not found: "
                                                        + passengerDto.returnSeatId()
                                        )
                                );


                validateSeat(
                        returnSeat,
                        returnFlight
                );

                bookingSeatMustBeAvailable(returnSeat);


                booking.addSeat(
                        returnSeat,
                        passenger
                );


                totalPrice =
                        totalPrice.add(
                                returnSeat.getPrice()
                        );
            } else if (passengerDto.returnSeatId() != null) {
                throw new InvalidTripException(
                        "Return seat is not allowed for a one-way booking"
                );
            }
        }


        // ==================================================
        // TOTAL PRICE
        // ==================================================

        booking.setTotalPrice(totalPrice);


        // ==================================================
        // SAVE
        // ==================================================

        Booking saved =
                bookingRepository.save(booking);


        return bookingMapper.toDto(saved);
    }


    private Passenger createPassenger(
            PassengerRequestDto passengerDto
    ) {

        return passengerRepository
                .findByPassportNumber(
                        passengerDto.passportNumber()
                )
                .orElseGet(() ->
                        passengerRepository.save(
                                passengerMapper.toEntity(
                                        passengerDto
                                )
                        )
                );
    }


    private String generateBookingReference() {

        return "FB-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }


    private void validateReturnFlight(
            Flight outboundFlight,
            Flight returnFlight
    ) {

        if (outboundFlight.getId().equals(returnFlight.getId())) {
            throw new InvalidTripException(
                    "Outbound and return flights must be different"
            );
        }

        if (!returnFlight.getDepartureTime()
                .isAfter(outboundFlight.getArrivalTime())) {
            throw new InvalidTripException(
                    "Return flight must depart after the outbound flight arrives"
            );
        }
    }


    private void ensureSeatWasNotSelectedTwice(
            Set<Long> selectedSeatIds,
            Long seatId
    ) {

        if (!selectedSeatIds.add(seatId)) {
            throw new IllegalArgumentException(
                    "Seat was selected more than once: " + seatId
            );
        }
    }


    private void validateFlightCanBeBooked(
            Flight flight
    ) {

        if (flight.getStatus() == FlightStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Flight has been cancelled"
            );
        }

        if (flight.getDepartureTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "Flight has already departed"
            );
        }
    }


    private void validateSeat(
            FlightSeat seat,
            Flight flight
    ) {

        if (!seat.getFlight()
                .getId()
                .equals(flight.getId())) {

            throw new IllegalArgumentException(
                    "Seat does not belong to this flight"
            );
        }
    }


    private void bookingSeatMustBeAvailable(
            FlightSeat seat
    ) {

        boolean booked =
                bookingSeatRepository
                        .existsBySeatIdAndBookingStatus(
                                seat.getId(),
                                BookingStatus.CONFIRMED
                        );

        if (booked) {

            throw new IllegalStateException(
                    "Seat "
                            + seat.getSeatNumber()
                            + " is already booked"
            );
        }
    }

    // FIND BOOKINGS BY EMAIL
    @Override
    public List<BookingResponseDto> getBookingsByEmail(String email){

        return bookingRepository.findBookingsByPassengerEmail(email, BookingStatus.CANCELLED)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    // FIND SINGLE BOOKING
    @Override
    public BookingResponseDto getBookingById(Long id){
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingWithSeats(Long bookingId) {

        Booking booking = bookingRepository
                .findBookingWithSeats(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getMyBookings() {

        String email = getAuthenticatedEmail();

        return bookingRepository
                .findByUser_Email(email)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getMyBookings(
            boolean archived
    ) {
        String email = getAuthenticatedEmail();

        List<Booking> bookings = archived
                ? bookingRepository
                  .findByUser_EmailAndArchivedAtIsNotNullOrderByBookingDateDesc(email)
                : bookingRepository
                  .findByUser_EmailAndArchivedAtIsNullOrderByBookingDateDesc(email);

        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    // CANCEL BOOKING
    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {

        String email = getAuthenticatedEmail();

        Booking booking = bookingRepository
                .findByIdAndUser_Email(bookingId, email)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        if (booking.getArchivedAt() != null) {
            throw new IllegalStateException(
                    "An archived booking cannot be cancelled"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking is already cancelled"
            );
        }

        if (booking.getOutboundFlight()
                .getDepartureTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "A departed booking cannot be cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        // No BookingSeat deletion is required.
        // Status-based availability releases the seats.
    }

    @Override
    @Transactional
    public void archiveBooking(Long bookingId) {

        String email = getAuthenticatedEmail();

        Booking booking = bookingRepository
                .findByIdAndUser_Email(bookingId, email)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Only cancelled bookings can be archived"
            );
        }

        if (booking.getArchivedAt() != null) {
            throw new IllegalStateException(
                    "Booking is already archived"
            );
        }

        booking.setArchivedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void restoreArchivedBooking(Long bookingId) {

        String email = getAuthenticatedEmail();

        Booking booking = bookingRepository
                .findByIdAndUser_Email(bookingId, email)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        booking.setArchivedAt(null);
    }

}