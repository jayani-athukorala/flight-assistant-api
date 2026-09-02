package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByPassengersEmail(String email);

    Optional<Booking> findByOutboundFlightIdAndPassengersEmail(
            Long flightId,
            String email
    );


        @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN FETCH b.passengers p
        LEFT JOIN FETCH b.outboundFlight
        LEFT JOIN FETCH b.returnFlight
        WHERE p.email = :email
        AND b.status <> :status
        """)
        List<Booking> findBookingsByPassengerEmail(
                @Param("email") String email,
                @Param("status") BookingStatus status
        );


@Query("""
    SELECT DISTINCT b
    FROM Booking b
    LEFT JOIN FETCH b.seats
    WHERE b.id = :bookingId
    """)
Optional<Booking> findBookingWithSeats(
        @Param("bookingId") Long bookingId
);

    Optional<Booking> findByIdAndUser_Email(
            Long bookingId,
            String email
    );

    List<Booking> findByUser_Email(String email);

    List<Booking> findByUser_EmailAndArchivedAtIsNullOrderByBookingDateDesc(
            String email
    );

    List<Booking> findByUser_EmailAndArchivedAtIsNotNullOrderByBookingDateDesc(
            String email
    );
}