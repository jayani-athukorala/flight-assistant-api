package se.lexicon.flightbooking_api.entity;

import jakarta.persistence.*;
import lombok.*;
import se.lexicon.flightbooking_api.entity.audit.Auditable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable =false, unique = true)
    private String passportNumber;

    @Column(nullable = false)
    private String email;

    @Builder.Default
    @ManyToMany(mappedBy = "passengers")
    private List<Booking> bookings = new ArrayList<>();

}