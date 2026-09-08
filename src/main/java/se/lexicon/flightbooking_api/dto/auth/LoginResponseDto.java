package se.lexicon.flightbooking_api.dto.auth;

import se.lexicon.flightbooking_api.entity.enums.UserRole;

public record LoginResponseDto(

        String token,

        String email,

        UserRole role

) {
}
