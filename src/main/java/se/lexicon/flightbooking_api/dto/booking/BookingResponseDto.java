package se.lexicon.flightbooking_api.dto.booking;


import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.passenger.PassengerDto;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.entity.enums.TripType;


import java.util.List;


public record BookingResponseDto(

        Long id,

        String bookingReference,

        BookingStatus status,

        TripType tripType,

        FlightDto outboundFlight,

        FlightDto returnFlight,

        Double totalPrice,

        List<PassengerDto> passengers,

        List<FlightSeatDto> seats

) {

}