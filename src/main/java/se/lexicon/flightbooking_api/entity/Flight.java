package se.lexicon.flightbooking_api.entity;

import jakarta.persistence.*;
import lombok.*;
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
public class Flight {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String flightNumber;


    @Column(nullable = false)
    private String airline;


    @Column(nullable = false)
    private String origin;


    @Column(nullable = false)
    private String destination;


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

}