package se.lexicon.flightbooking_api.dto.airport;

public record AirportResponseDto(
        Long id,
        String code,
        String name,
        String city,
        String country
) {}
