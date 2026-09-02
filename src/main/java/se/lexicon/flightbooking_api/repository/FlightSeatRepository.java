package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.lexicon.flightbooking_api.entity.FlightSeat;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.entity.BookingSeat;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeat, Long> {

    List<FlightSeat> findByFlightId(Long flightId);

    List<FlightSeat> findByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);

    @Query("""
        SELECT s
        FROM FlightSeat s
        WHERE s.flight.id = :flightId
        AND s.seatClass = :seatClass
        AND NOT EXISTS (
            SELECT bs
            FROM BookingSeat bs
            WHERE bs.seat.id = s.id
            AND bs.booking.status = BookingStatus.CONFIRMED
        )
    """)
    List<FlightSeat> findAvailableSeatsByClass(Long flightId, SeatClass seatClass);

    Optional<FlightSeat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}