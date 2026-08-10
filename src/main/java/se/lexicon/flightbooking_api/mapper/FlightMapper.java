package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.entity.Flight;

@Mapper(
        componentModel = "spring",
        imports = java.util.Objects.class
)
public interface FlightMapper {

    @Mapping(
        target = "startingPrice",
        expression = "java(flight.getSeats().stream()" +
                     ".map(seat -> seat.getPrice())" +
                     ".filter(Objects::nonNull)" +
                     ".min(Double::compareTo)" +
                     ".orElse(null))"
    )
    FlightDto toDto(Flight flight);
}