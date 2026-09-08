package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.flight.CreateFlightDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import java.util.List;
import java.time.LocalDate;

public interface FlightService {

    List<FlightDto> getAllFlights();

    List<FlightDto> searchFlights(
            LocalDate date,
            FlightStatus status,
            String query,
            String createdByEmail
    );

    List<FlightDto> getAvailableFlights();

    List<FlightDto> getAvailableFlights(
            Long originId,
            Long destinationId
    );

    List<FlightDto> getAvailableFlights(
            Long originId,
            Long destinationId,
            LocalDate date
    );

    FlightDto getFlightById(Long id);

    FlightDto createFlight(CreateFlightDto request);

    void deleteFlight(Long id);

    void updateFlightStatuses();
}
