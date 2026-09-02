package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.passenger.PassengerRequestDto;
import se.lexicon.flightbooking_api.dto.passenger.PassengerResponseDto;
import se.lexicon.flightbooking_api.entity.Passenger;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    Passenger toEntity(PassengerRequestDto dto);

    PassengerResponseDto toDto(Passenger passenger);

    List<PassengerResponseDto> toDtoList(
            List<Passenger> passengers
    );
}