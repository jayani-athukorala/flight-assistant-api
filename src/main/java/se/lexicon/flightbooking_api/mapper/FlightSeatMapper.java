package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.FlightSeat;

@Mapper(componentModel = "spring")
public interface FlightSeatMapper {

    @Mapping(target = "id", source = "seat.id")
    @Mapping(
            target = "seatNumber",
            source = "seat.seatNumber"
    )
    @Mapping(
            target = "seatClass",
            source = "seat.seatClass"
    )
    @Mapping(target = "price", source = "seat.price")
    @Mapping(target = "available", source = "available")
    FlightSeatDto toDto(
            FlightSeat seat,
            boolean available
    );
}