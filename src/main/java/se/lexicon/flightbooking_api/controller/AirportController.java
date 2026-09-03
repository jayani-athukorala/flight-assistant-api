package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.service.AirportService;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @GetMapping("/search")
    @Operation(
            summary = "Search airports",
            description = "Searches by IATA code, airport name, city, or country"
    )
    public ResponseEntity<List<AirportResponseDto>> searchAirports(
            @RequestParam(name = "q") String query,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return ResponseEntity.ok(
                airportService.search(query, limit)
        );
    }
}
