package se.lexicon.flightbooking_api.dto.passenger;

public record PassengerResponseDto(
        Long id,
        String firstName,
        String lastName,
        String passportNumber,
        String email
) {}