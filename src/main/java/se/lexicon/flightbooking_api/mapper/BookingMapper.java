package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.entity.Booking;

@Mapper(
        componentModel = "spring",
        uses = {
                FlightMapper.class,
                BookingSeatMapper.class,
                PassengerMapper.class
        }
)
public interface BookingMapper {

    @Mapping(target = "createdByEmail", source = "createdBy.email")
    BookingResponseDto toDto(Booking booking);
}
