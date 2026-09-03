package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository
        extends JpaRepository<Flight, Long> {

    List<Flight> findByFlightNumber(String flightNumber);

    @EntityGraph(attributePaths = {
            "origin",
            "destination",
            "seats"
    })
    List<Flight> findByStatus(FlightStatus status);

    @EntityGraph(attributePaths = {
            "origin",
            "destination",
            "seats"
    })
    List<Flight> findByStatusAndOrigin_IdAndDestination_Id(
            FlightStatus status,
            Long originId,
            Long destinationId
    );

    List<Flight> findByOriginAndDestination(
            Airport origin,
            Airport destination
    );

    List<Flight> findByOrigin_CodeIgnoreCase(
            String originCode
    );

    List<Flight> findByDestination_CodeIgnoreCase(
            String destinationCode
    );

    @EntityGraph(attributePaths = {
            "origin",
            "destination",
            "seats"
    })
    List<Flight>
    findByStatusAndOrigin_CodeIgnoreCaseAndDestination_CodeIgnoreCase(
            FlightStatus status,
            String originCode,
            String destinationCode
    );

    List<Flight> findByStatusAndDepartureTimeBefore(
            FlightStatus status,
            LocalDateTime departureTime
    );
}