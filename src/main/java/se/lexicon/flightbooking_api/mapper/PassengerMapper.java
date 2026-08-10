package se.lexicon.flightbooking_api.mapper;


import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.passenger.PassengerDto;
import se.lexicon.flightbooking_api.entity.Passenger;


@Mapper(
        componentModel = "spring"
)
public interface PassengerMapper {


    PassengerDto toDto(Passenger passenger);

    Passenger toEntity(PassengerDto dto);

}