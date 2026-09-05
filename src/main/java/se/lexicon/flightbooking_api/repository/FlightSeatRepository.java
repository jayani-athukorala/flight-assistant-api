package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.lexicon.flightbooking_api.entity.FlightSeat;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.entity.BookingSeat;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSeatRepository
        extends JpaRepository<FlightSeat, Long> {

    List<FlightSeat> findByFlight_Id(Long flightId);

    @Query("""
        SELECT seat
        FROM FlightSeat seat
        WHERE seat.flight.id = :flightId
          AND NOT EXISTS (
              SELECT bookingSeat.id
              FROM BookingSeat bookingSeat
              WHERE bookingSeat.seat = seat
                AND bookingSeat.booking.status IN :blockingStatuses
          )
        ORDER BY seat.seatNumber
    """)
    List<FlightSeat> findAvailableSeats(
            @Param("flightId") Long flightId,
            @Param("blockingStatuses")
            Collection<BookingStatus> blockingStatuses
    );

    @Query("""
        SELECT seat
        FROM FlightSeat seat
        WHERE seat.flight.id = :flightId
          AND seat.seatClass = :seatClass
          AND NOT EXISTS (
              SELECT bookingSeat.id
              FROM BookingSeat bookingSeat
              WHERE bookingSeat.seat = seat
                AND bookingSeat.booking.status IN :blockingStatuses
          )
        ORDER BY seat.seatNumber
    """)
    List<FlightSeat> findAvailableSeatsByClass(
            @Param("flightId") Long flightId,
            @Param("seatClass") SeatClass seatClass,
            @Param("blockingStatuses")
            Collection<BookingStatus> blockingStatuses
    );

    Optional<FlightSeat> findByFlight_IdAndSeatNumber(
            Long flightId,
            String seatNumber
    );
}