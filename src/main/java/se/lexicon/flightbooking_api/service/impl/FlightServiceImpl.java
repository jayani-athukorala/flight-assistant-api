package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.flight.*;

import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import se.lexicon.flightbooking_api.exception.FlightNotFoundException;

import se.lexicon.flightbooking_api.mapper.CreateFlightMapper;
import se.lexicon.flightbooking_api.mapper.FlightMapper;

import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;
import se.lexicon.flightbooking_api.service.FlightService;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final FlightMapper flightMapper;
    private final CreateFlightMapper createFlightMapper;

    @Override
    public List<FlightDto> getAllFlights(){
        return flightRepository.findAll()
                .stream()
                .map(flightMapper::toDto)
                .toList();
    }

    @Override
    public List<FlightDto> getAvailableFlights(){

        return flightRepository
                .findByStatus(FlightStatus.SCHEDULED)
                .stream()
                .map(flightMapper::toDto)
                .toList();

    }

    @Override
    public FlightDto getFlightById(Long id){
        Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new FlightNotFoundException(id));
        return flightMapper.toDto(flight);

    }

    @Transactional
    public FlightDto createFlight(CreateFlightDto request) {

        if (request.originAirportId()
                .equals(request.destinationAirportId())) {
            throw new IllegalArgumentException(
                    "Origin and destination must be different"
            );
        }

        Airport origin = airportRepository
                .findById(request.originAirportId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Origin airport not found: "
                                        + request.originAirportId()
                        )
                );

        Airport destination = airportRepository
                .findById(request.destinationAirportId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Destination airport not found: "
                                        + request.destinationAirportId()
                        )
                );

        Flight flight = createFlightMapper.toEntity(request);

        flight.setOrigin(origin);
        flight.setDestination(destination);

        Flight saved = flightRepository.save(flight);

        return flightMapper.toDto(saved);
    }

    @Override
    public void deleteFlight(Long id){
        Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new FlightNotFoundException(id));
        flightRepository.delete(flight);

    }

    @Transactional
    public void updateDepartedFlights() {

        LocalDateTime now = LocalDateTime.now();

        List<Flight> flights =
                flightRepository.findByStatusAndDepartureTimeBefore(
                        FlightStatus.SCHEDULED,
                        now
                );

        flights.forEach(
                flight -> flight.setStatus(FlightStatus.DEPARTED)
        );

        flightRepository.saveAll(flights);
    }

}