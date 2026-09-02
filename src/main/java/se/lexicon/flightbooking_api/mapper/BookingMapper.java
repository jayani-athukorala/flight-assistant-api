package se.lexicon.flightbooking_api.mapper;

import org.mapstruct.Mapper;
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

    BookingResponseDto toDto(Booking booking);
}