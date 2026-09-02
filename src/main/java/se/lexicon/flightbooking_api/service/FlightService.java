package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.flight.CreateFlightDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;

import java.util.List;

public interface FlightService {

    List<FlightDto> getAllFlights();
    List<FlightDto> getAvailableFlights();
    FlightDto getFlightById(Long id);
    FlightDto createFlight(CreateFlightDto dto);
    void deleteFlight(Long id);
    void updateDepartedFlights();
}
