package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.auth.LoginRequestDto;
import se.lexicon.flightbooking_api.dto.auth.LoginResponseDto;
import se.lexicon.flightbooking_api.dto.auth.RegisterRequestDto;
import se.lexicon.flightbooking_api.entity.User;
import se.lexicon.flightbooking_api.entity.enums.UserRole;
import se.lexicon.flightbooking_api.repository.UserRepository;
import se.lexicon.flightbooking_api.security.JwtService;
import se.lexicon.flightbooking_api.security.CustomUserDetailsService;
import se.lexicon.flightbooking_api.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService userDetailsService;

    private final JwtService jwtService;

    @Override
    @Transactional
    public void register(
            RegisterRequestDto request
    ) {

        if (
                userRepository.existsByEmail(
                        request.email()
                )
        ) {

            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        User user =
                User.builder()
                        .email(request.email())
                        .password(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .role(UserRole.USER)
                        .build();

        userRepository.save(user);
    }

    @Override
    public LoginResponseDto login(
            LoginRequestDto request
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(
                                request.email()
                        );

        String token =
                jwtService.generateToken(
                        userDetails
                );

        return new LoginResponseDto(
                token,
                userDetails.getUsername()
        );
    }
}