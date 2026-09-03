package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.flight.CreateFlightDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;

import java.util.List;

public interface FlightService {

    List<FlightDto> getAllFlights();

    List<FlightDto> getAvailableFlights();

    List<FlightDto> getAvailableFlights(
            Long originId,
            Long destinationId
    );

    FlightDto getFlightById(Long id);

    FlightDto createFlight(CreateFlightDto request);

    void deleteFlight(Long id);

    void updateDepartedFlights();
}