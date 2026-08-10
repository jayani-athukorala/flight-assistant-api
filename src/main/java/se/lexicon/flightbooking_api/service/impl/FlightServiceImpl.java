package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import se.lexicon.flightbooking_api.dto.flight.*;

import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import se.lexicon.flightbooking_api.exception.FlightNotFoundException;

import se.lexicon.flightbooking_api.mapper.CreateFlightMapper;
import se.lexicon.flightbooking_api.mapper.FlightMapper;

import se.lexicon.flightbooking_api.repository.FlightRepository;
import se.lexicon.flightbooking_api.service.FlightService;


import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
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
                .findByStatus(FlightStatus.AVAILABLE)
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

    @Override
    public FlightDto createFlight(CreateFlightDto dto){
        Flight flight = createFlightMapper.toEntity(dto);
        flight.setStatus(FlightStatus.AVAILABLE);
        return flightMapper.toDto(flightRepository.save(flight));
    }

    @Override
    public void deleteFlight(Long id){
        Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new FlightNotFoundException(id));
        flightRepository.delete(flight);

    }

}