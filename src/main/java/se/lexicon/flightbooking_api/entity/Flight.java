package se.lexicon.flightbooking_api.entity;

import jakarta.persistence.*;
import lombok.*;
import se.lexicon.flightbooking_api.entity.audit.Auditable;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Flight number can repeat across different flight instances/dates.
     * Example: EK101 can operate on multiple days.
     */
    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private String airline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_airport_id", nullable = false)
    private Airport origin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private Airport destination;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @Builder.Default
    @OneToMany(
            mappedBy = "flight",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FlightSeat> seats = new ArrayList<>();

    public void addSeat(FlightSeat seat) {
        seats.add(seat);
        seat.setFlight(this);
    }

    public void removeSeat(FlightSeat seat) {
        seats.remove(seat);
        seat.setFlight(null);
    }
}