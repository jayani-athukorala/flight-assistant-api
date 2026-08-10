package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.FlightSeat;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.exception.SeatUnavailableException;
import se.lexicon.flightbooking_api.mapper.FlightSeatMapper;
import se.lexicon.flightbooking_api.repository.FlightSeatRepository;
import se.lexicon.flightbooking_api.service.FlightSeatService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightSeatServiceImpl implements FlightSeatService {


    private final FlightSeatRepository seatRepository;

    private final FlightSeatMapper mapper;



    @Override
    public List<FlightSeatDto> getAvailableSeats(Long flightId){

        return seatRepository
                .findByFlightId(flightId)
                .stream()
                .map(mapper::toDto)
                .toList();

    }



    @Override
    public List<FlightSeatDto> getAvailableSeatsByClass(
            Long flightId,
            SeatClass seatClass
    ){

        return seatRepository
                .findByFlightIdAndSeatClass(
                        flightId,
                        seatClass
                )
                .stream()
                .map(mapper::toDto)
                .toList();

    }


//    @Override
//    @Transactional
//    public void reserveSeat(
//            Long seatId
//    ){
//
//        FlightSeat seat =
//                seatRepository.findById(seatId)
//                        .orElseThrow();
//
//
//        /*
//         * Reservation logic will move to BookingSeat.
//         * A FlightSeat itself is never assigned to a booking.
//         */
//
//
//    }



    @Override
    @Transactional
    public void releaseSeat(
            Long seatId
    ){

        /*
         * Nothing to release here anymore.
         *
         * The booking-seat relation
         * will be removed instead.
         */

    }

}