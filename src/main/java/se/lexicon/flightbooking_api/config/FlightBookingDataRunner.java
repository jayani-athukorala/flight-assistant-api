package se.lexicon.flightbooking_api.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.entity.User;
import se.lexicon.flightbooking_api.entity.enums.UserRole;
import se.lexicon.flightbooking_api.repository.UserRepository;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class FlightBookingDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {

        createUserIfNotExists(
                "john@test.com",
                "password123",
                UserRole.USER
        );

        createUserIfNotExists(
                "jane@test.com",
                "password123",
                UserRole.USER
        );

        createUserIfNotExists(
                "admin@test.com",
                "admin123",
                UserRole.ADMIN
        );
    }

    private void createUserIfNotExists(
            String email,
            String password,
            UserRole role
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);

        IO.println("Created " + role + " user: " + email);
    }
}