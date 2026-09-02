package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.entity.Airport;

@Mapper(componentModel = "spring")
public interface AirportMapper {

    AirportResponseDto toDto(Airport airport);
}
