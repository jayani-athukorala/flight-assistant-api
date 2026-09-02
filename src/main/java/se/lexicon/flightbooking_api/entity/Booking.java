package se.lexicon.flightbooking_api.entity;

import jakarta.persistence.*;
import lombok.*;
import se.lexicon.flightbooking_api.entity.audit.Auditable;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.entity.enums.TripType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    private TripType tripType;

    @ManyToOne
    @JoinColumn(name = "outbound_flight_id")
    private Flight outboundFlight;

    @ManyToOne
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "booking_passengers",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "passenger_id")
    )
    private List<Passenger> passengers = new ArrayList<>();


    @OneToMany(
        mappedBy = "booking",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<BookingSeat> seats = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    private LocalDateTime archivedAt;

    public void addPassenger(Passenger passenger){
        passengers.add(passenger);
        passenger.getBookings().add(this);
    }

    public void addSeat(
            FlightSeat seat,
            Passenger passenger
    ) {
        BookingSeat bookingSeat = BookingSeat.builder()
                .booking(this)
                .seat(seat)
                .passenger(passenger)
                .build();

        seats.add(bookingSeat);
    }
}