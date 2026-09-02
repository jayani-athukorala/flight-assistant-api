package se.lexicon.flightbooking_api.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.lexicon.flightbooking_api.entity.Airport;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    Optional<Airport> findByCodeIgnoreCase(String code);

    @Query("""
            SELECT airport
            FROM Airport airport
            WHERE LOWER(airport.code) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(airport.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(airport.city) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(airport.country) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY
                CASE WHEN LOWER(airport.code) = LOWER(:query) THEN 0 ELSE 1 END,
                airport.city,
                airport.name
            """)
    List<Airport> search(
            @Param("query") String query,
            Pageable pageable
    );
}

