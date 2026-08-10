package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.flight.CreateFlightDto;
import se.lexicon.flightbooking_api.entity.Flight;

@Mapper(componentModel = "spring")
public interface CreateFlightMapper {


    @Mapping(target="id", ignore=true)
    @Mapping(target="status", ignore=true)
    @Mapping(target="seats", ignore=true)
    Flight toEntity(CreateFlightDto dto);

}