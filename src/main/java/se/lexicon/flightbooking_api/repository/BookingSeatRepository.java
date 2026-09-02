package se.lexicon.flightbooking_api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.lexicon.flightbooking_api.entity.BookingSeat;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;

public interface BookingSeatRepository
        extends JpaRepository<BookingSeat, Long> {

    @Query("""
        SELECT COUNT(bs) > 0
        FROM BookingSeat bs
        WHERE bs.seat.id = :seatId
        AND bs.booking.status = :status
    """)
    boolean existsBySeatIdAndBookingStatus(
            @Param("seatId") Long seatId,
            @Param("status") BookingStatus status
    );

    @Query("""
        SELECT bs.seat.id
        FROM BookingSeat bs
        WHERE bs.seat.flight.id = :flightId
        AND bs.booking.status = :status
    """)
    List<Long> findBookedSeatIdsForFlight(
            @Param("flightId") Long flightId,
            @Param("status") BookingStatus status
    );

    @Query("""
        SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END
        FROM BookingSeat bs
        WHERE bs.seat.id = :seatId
          AND bs.booking.status IN :statuses
    """)
    boolean existsActiveBookingForSeat(
            @Param("seatId") Long seatId,
            @Param("statuses") Collection<BookingStatus> statuses
    );
}