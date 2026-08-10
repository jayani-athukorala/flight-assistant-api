package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.lexicon.flightbooking_api.entity.Passenger;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    Optional<Passenger> findByPassportNumber(String passportNumber);

    List<Passenger> findByEmail(String email);

    boolean existsByPassportNumber(String passportNumber);
}