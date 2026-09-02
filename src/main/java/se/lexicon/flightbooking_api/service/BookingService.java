package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.booking.*;
import se.lexicon.flightbooking_api.entity.BookingSeat;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto request);
    List<BookingResponseDto> getBookingsByEmail(String email);
    BookingResponseDto getBookingById(Long id);
    BookingResponseDto getBookingWithSeats(Long bookingId);
    List<BookingResponseDto> getMyBookings();
    List<BookingResponseDto> getMyBookings(boolean archived);
    void cancelBooking(Long bookingId);
    void archiveBooking(Long bookingId);
    void restoreArchivedBooking(Long bookingId);
}