package se.lexicon.flightbooking_api.mapper;

import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.FlightSeat;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(target = "createdByEmail", source = "createdBy.email")
    @Mapping(
            target = "startingPrice",
            expression = "java(calculateStartingPrice(flight))"
    )
    FlightDto toDto(Flight flight);

    List<FlightDto> toDtoList(List<Flight> flights);

    default BigDecimal calculateStartingPrice(Flight flight) {
        if (
                flight == null ||
                        flight.getSeats() == null ||
                        !Hibernate.isInitialized(flight.getSeats()) ||
                        flight.getSeats().isEmpty()
        ) {
            return null;
        }

        return flight.getSeats()
                .stream()
                .map(FlightSeat::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }
}
