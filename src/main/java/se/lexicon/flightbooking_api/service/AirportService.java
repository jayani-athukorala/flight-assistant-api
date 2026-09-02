package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;

import java.util.List;

public interface AirportService {
    List<AirportResponseDto> search(String query, int requestedLimit);
}
