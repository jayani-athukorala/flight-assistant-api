package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.entity.*;
import se.lexicon.flightbooking_api.entity.enums.*;
import se.lexicon.flightbooking_api.exception.*;

import se.lexicon.flightbooking_api.mapper.BookingMapper;
import se.lexicon.flightbooking_api.mapper.PassengerMapper;

import se.lexicon.flightbooking_api.repository.*;

import se.lexicon.flightbooking_api.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final PassengerMapper passengerMapper;

    private String getAuthenticatedEmail() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        return authentication.getName();
    }

    // CREATE BOOKING
    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {

        String email = getAuthenticatedEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email)
                );

        // -------------------------------------------------
        // OUTBOUND FLIGHT
        // -------------------------------------------------

        Flight outboundFlight = flightRepository.findById(
                request.outboundFlightId()
        ).orElseThrow(() ->
                new FlightNotFoundException(request.outboundFlightId())
        );

        // -------------------------------------------------
        // RETURN FLIGHT
        // -------------------------------------------------

        Flight returnFlight = null;

        if (request.tripType() == TripType.ROUND_TRIP) {

            if (request.returnFlightId() == null) {
                throw new InvalidTripException(
                        "Round trip requires a return flight"
                );
            }

            returnFlight = flightRepository.findById(
                    request.returnFlightId()
            ).orElseThrow(() ->
                    new FlightNotFoundException(request.returnFlightId())
            );
        }

        // -------------------------------------------------
        // CREATE BOOKING
        // -------------------------------------------------

        Booking booking = Booking.builder()
                .user(user)
                .bookingReference(generateBookingReference())
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .tripType(request.tripType())
                .outboundFlight(outboundFlight)
                .returnFlight(returnFlight)
                .totalPrice(0.0)
                .build();

        // -------------------------------------------------
        // PASSENGERS
        // -------------------------------------------------

        for (var passengerDto : request.passengers()) {

            if (passengerRepository.existsByPassportNumber(
                    passengerDto.passportNumber()
            )) {
                throw new PassengerAlreadyExistsException(
                        passengerDto.passportNumber()
                );
            }

            Passenger passenger =
                    passengerMapper.toEntity(passengerDto);

            booking.addPassenger(passenger);
        }

        // -------------------------------------------------
        // PRICE
        // -------------------------------------------------

        double totalPrice =
                calculatePrice(
                        outboundFlight,
                        request.seatClass()
                );

        if (returnFlight != null) {

            totalPrice += calculatePrice(
                    returnFlight,
                    request.seatClass()
            );
        }

        booking.setTotalPrice(totalPrice);

        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        Booking saved = bookingRepository.save(booking);

        return bookingMapper.toDto(saved);
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

    // CANCEL BOOKING
    @Transactional
    @Override
    public void cancelBooking(Long bookingId) {

        String email = getAuthenticatedEmail();

        Booking booking = bookingRepository
                .findByIdAndUser_Email(bookingId, email)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking is already cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
    }

    private double calculatePrice(Flight flight, SeatClass seatClass){
        return switch(seatClass){
            case ECONOMY -> 100.0;
            case PREMIUM_ECONOMY -> 180.0;
            case BUSINESS -> 400.0;
            case FIRST_CLASS -> 800.0;
        };

    }

    private String generateBookingReference(){
        return "FB-" + UUID.randomUUID().toString()
                        .substring(0,8)
                        .toUpperCase();
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


}