package se.lexicon.flightbooking_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.entity.User;
import se.lexicon.flightbooking_api.repository.UserRepository;

import java.util.Optional;

@Component("auditorAware")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware
        implements AuditorAware<User> {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.empty();
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email);
    }
}