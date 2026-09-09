package se.lexicon.flightbooking_api.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.entity.User;

import java.util.Optional;

@Component("auditorAware")
public class SpringSecurityAuditorAware
        implements AuditorAware<User> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        if (!(authentication.getPrincipal()
                instanceof AuthenticatedUser authenticatedUser)) {
            return Optional.empty();
        }

        /*
         * getReference does not execute a repository query.
         * It creates a managed reference using the known user ID,
         * avoiding recursive Hibernate flushing.
         */
        User auditor = entityManager.getReference(
                User.class,
                authenticatedUser.id()
        );

        return Optional.of(auditor);
    }
}