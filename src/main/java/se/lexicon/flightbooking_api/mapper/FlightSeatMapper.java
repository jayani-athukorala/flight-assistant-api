package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.FlightSeat;


@Mapper(componentModel = "spring")
public interface FlightSeatMapper {


    FlightSeatDto toDto(FlightSeat seat);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flight", ignore = true)
    FlightSeat toEntity(FlightSeatDto dto);

}