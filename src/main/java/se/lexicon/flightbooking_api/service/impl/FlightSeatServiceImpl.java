package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.mapper.FlightSeatMapper;
import se.lexicon.flightbooking_api.repository.FlightSeatRepository;
import se.lexicon.flightbooking_api.service.FlightSeatService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightSeatServiceImpl implements FlightSeatService {

    private final FlightSeatRepository flightSeatRepository;
    private final FlightSeatMapper flightSeatMapper;

    @Override
    public List<FlightSeatDto> getAvailableSeats(
            Long flightId
    ) {
        return flightSeatRepository
                .findAvailableSeats(
                        flightId,
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.CONFIRMED
                        )
                )
                .stream()
                .map(seat ->
                        flightSeatMapper.toDto(
                                seat,
                                true
                        )
                )
                .toList();
    }

    @Override
    public List<FlightSeatDto> getAvailableSeatsByClass(
            Long flightId,
            SeatClass seatClass
    ) {
        return flightSeatRepository
                .findAvailableSeatsByClass(
                        flightId,
                        seatClass,
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.CONFIRMED
                        )
                )
                .stream()
                .map(seat ->
                        flightSeatMapper.toDto(
                                seat,
                                true
                        )
                )
                .toList();
    }
}