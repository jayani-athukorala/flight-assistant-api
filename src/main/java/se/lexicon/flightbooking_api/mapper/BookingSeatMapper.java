package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.seat.BookingSeatDto;
import se.lexicon.flightbooking_api.entity.BookingSeat;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingSeatMapper {

    @Mapping(
            target = "passengerId",
            source = "passenger.id"
    )
    @Mapping(
            target = "flightId",
            source = "seat.flight.id"
    )
    @Mapping(
            target = "flightSeatId",
            source = "seat.id"
    )
    @Mapping(
            target = "seatNumber",
            source = "seat.seatNumber"
    )
    @Mapping(
            target = "seatClass",
            source = "seat.seatClass"
    )
    @Mapping(
            target = "price",
            source = "seat.price"
    )
    BookingSeatDto toDto(BookingSeat bookingSeat);

    List<BookingSeatDto> toDtoList(
            List<BookingSeat> bookingSeats
    );
}