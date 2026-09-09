package se.lexicon.flightbooking_api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AuthenticatedUser(
        Long id,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public AuthenticatedUser {
        authorities = authorities == null
                ? List.of()
                : List.copyOf(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
