package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;

import java.util.List;

public interface FlightSeatService {

    List<FlightSeatDto> getAvailableSeats(Long flightId);
    List<FlightSeatDto> getAvailableSeatsByClass(Long flightId, SeatClass seatClass);
//    void reserveSeat(Long seatId, Booking booking);
    void releaseSeat(Long seatId);

}