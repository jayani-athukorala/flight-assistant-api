package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import se.lexicon.flightbooking_api.dto.flight.*;

import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.FlightSeat;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;

import se.lexicon.flightbooking_api.exception.FlightNotFoundException;

import se.lexicon.flightbooking_api.mapper.CreateFlightMapper;
import se.lexicon.flightbooking_api.mapper.FlightMapper;

import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;
import se.lexicon.flightbooking_api.service.FlightService;


import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final FlightMapper flightMapper;
    private final CreateFlightMapper createFlightMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FlightDto> getAllFlights() {
        return flightMapper.toDtoList(
                flightRepository.findAll()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDto> getAvailableFlights() {
        return flightMapper.toDtoList(
                flightRepository.findByStatus(
                        FlightStatus.SCHEDULED
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDto> getAvailableFlights(
            Long originId,
            Long destinationId
    ) {
        return getAvailableFlights(originId, destinationId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDto> searchFlights(
            LocalDate date,
            FlightStatus status,
            String query,
            String createdByEmail
    ) {
        Specification<Flight> specification = Specification.unrestricted();

        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            specification = specification.and((root, ignored, cb) ->
                    cb.and(
                            cb.greaterThanOrEqualTo(root.get("departureTime"), start),
                            cb.lessThan(root.get("departureTime"), end)
                    )
            );
        }

        if (status != null) {
            specification = specification.and((root, ignored, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase() + "%";
            specification = specification.and((root, ignored, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("flightNumber")), pattern),
                            cb.like(cb.lower(root.get("airline")), pattern),
                            cb.like(cb.lower(root.get("origin").get("code")), pattern),
                            cb.like(cb.lower(root.get("origin").get("city")), pattern),
                            cb.like(cb.lower(root.get("destination").get("code")), pattern),
                            cb.like(cb.lower(root.get("destination").get("city")), pattern)
                    )
            );
        }

        if (createdByEmail != null && !createdByEmail.isBlank()) {
            String pattern = "%" + createdByEmail.trim().toLowerCase() + "%";
            specification = specification.and((root, ignored, cb) ->
                    cb.like(cb.lower(root.get("createdBy").get("email")), pattern)
            );
        }

        return flightMapper.toDtoList(flightRepository.findAll(specification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDto> getAvailableFlights(
            Long originId,
            Long destinationId,
            LocalDate date
    ) {
        if (originId == null || destinationId == null) {
            throw new IllegalArgumentException(
                    "Origin and destination IDs are required"
            );
        }

        if (originId.equals(destinationId)) {
            throw new IllegalArgumentException(
                    "Origin and destination must be different"
            );
        }

        if (date == null) {
            return flightMapper.toDtoList(
                    flightRepository
                            .findByStatusAndOrigin_IdAndDestination_Id(
                                    FlightStatus.SCHEDULED,
                                    originId,
                                    destinationId
                            )
            );
        }

        return flightMapper.toDtoList(
                flightRepository
                        .findByStatusAndOrigin_IdAndDestination_IdAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThanOrderByDepartureTimeAsc(
                                FlightStatus.SCHEDULED,
                                originId,
                                destinationId,
                                date.atStartOfDay(),
                                date.plusDays(1).atStartOfDay()
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FlightDto getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(
                        () -> new FlightNotFoundException(id)
                );

        return flightMapper.toDto(flight);
    }


    @Override
    @Transactional
    public FlightDto createFlight(CreateFlightDto request) {

        if (request.originAirportId()
                .equals(request.destinationAirportId())) {
            throw new IllegalArgumentException(
                    "Origin and destination must be different"
            );
        }

        if (!request.arrivalTime().isAfter(request.departureTime())) {
            throw new IllegalArgumentException(
                    "Arrival time must be after departure time"
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
        flight.setStatus(FlightStatus.SCHEDULED);
        addGeneratedSeats(flight, request.economyBasePrice());

        Flight saved = flightRepository.save(flight);

        return flightMapper.toDto(saved);
    }

    @Override
    public void deleteFlight(Long id){
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        flightRepository.delete(flight);

    }

    @Override
    @Transactional
    public void updateFlightStatuses() {

        LocalDateTime now = LocalDateTime.now();

        List<Flight> flights = flightRepository.findAll();

        flights.stream()
                .filter(flight -> flight.getStatus() != FlightStatus.CANCELLED)
                .filter(flight -> flight.getStatus() != FlightStatus.COMPLETED)
                .forEach(flight -> {
                    if (!now.isBefore(flight.getArrivalTime())) {
                        flight.setStatus(FlightStatus.COMPLETED);
                    } else if (!now.isBefore(flight.getDepartureTime())) {
                        flight.setStatus(FlightStatus.DEPARTED);
                    } else if (!now.isBefore(flight.getDepartureTime().minusHours(2))) {
                        flight.setStatus(FlightStatus.BOARDING);
                    } else if (flight.getStatus() == FlightStatus.BOARDING) {
                        flight.setStatus(FlightStatus.SCHEDULED);
                    }
                });

        flightRepository.saveAll(flights);
    }

    private void addGeneratedSeats(Flight flight, BigDecimal economyBasePrice) {
        addSeat(flight, "1A", SeatClass.FIRST_CLASS, economyBasePrice, "3.20", "500.00");
        addSeat(flight, "2A", SeatClass.BUSINESS, economyBasePrice, "2.10", "250.00");
        addSeat(flight, "2B", SeatClass.BUSINESS, economyBasePrice, "2.10", "250.00");
        addSeat(flight, "5A", SeatClass.PREMIUM_ECONOMY, economyBasePrice, "1.45", "120.00");
        addSeat(flight, "5B", SeatClass.PREMIUM_ECONOMY, economyBasePrice, "1.45", "120.00");
        addSeat(flight, "10A", SeatClass.ECONOMY, economyBasePrice, "1.00", "80.00");
        addSeat(flight, "10B", SeatClass.ECONOMY, economyBasePrice, "1.00", "80.00");
        addSeat(flight, "11A", SeatClass.ECONOMY, economyBasePrice, "1.00", "50.00");
        addSeat(flight, "11B", SeatClass.ECONOMY, economyBasePrice, "1.00", "50.00");
        addSeat(flight, "12A", SeatClass.ECONOMY, economyBasePrice, "1.00", "20.00");
        addSeat(flight, "12B", SeatClass.ECONOMY, economyBasePrice, "1.00", "20.00");
        addSeat(flight, "12C", SeatClass.ECONOMY, economyBasePrice, "1.00", "0.00");
    }

    private void addSeat(
            Flight flight,
            String seatNumber,
            SeatClass seatClass,
            BigDecimal basePrice,
            String multiplier,
            String adjustment
    ) {
        BigDecimal price = basePrice
                .multiply(new BigDecimal(multiplier))
                .add(new BigDecimal(adjustment))
                .setScale(2, RoundingMode.HALF_UP);

        flight.addSeat(FlightSeat.builder()
                .seatNumber(seatNumber)
                .seatClass(seatClass)
                .price(price)
                .build());
    }

}
