package se.lexicon.flightbooking_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "airports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_airport_code",
                columnNames = "code"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @PrePersist
    @PreUpdate
    private void normalizeCode() {
        code = code == null ? null : code.trim().toUpperCase();
    }
}
