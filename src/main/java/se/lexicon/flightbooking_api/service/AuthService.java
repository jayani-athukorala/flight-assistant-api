package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.auth.LoginRequestDto;
import se.lexicon.flightbooking_api.dto.auth.LoginResponseDto;
import se.lexicon.flightbooking_api.dto.auth.RegisterRequestDto;

public interface AuthService {

    void register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request);
}