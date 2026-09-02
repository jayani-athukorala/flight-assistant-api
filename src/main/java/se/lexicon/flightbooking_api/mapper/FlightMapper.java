package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.entity.Flight;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Mapper(
        componentModel = "spring",
        uses = AirportMapper.class
)
public interface FlightMapper {

    FlightDto toDto(Flight flight);

    List<FlightDto> toDtoList(List<Flight> flights);
}