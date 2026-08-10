package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.booking.*;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto request);
    List<BookingResponseDto> getBookingsByEmail(String email);
    BookingResponseDto getBookingById(Long id);
    BookingResponseDto getBookingWithSeats(Long bookingId);
    List<BookingResponseDto> getMyBookings();
    void cancelBooking(Long bookingId);
}