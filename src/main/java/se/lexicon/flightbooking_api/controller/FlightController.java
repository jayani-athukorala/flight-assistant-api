package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.service.FlightService;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(
        name = "Flights",
        description = "Flight search and flight information"
)
public class FlightController {

    private final FlightService flightService;


    @GetMapping
    @Operation(
            summary = "Get all flights",
            description = "Returns all available flights in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Flights retrieved successfully"
            )
    })
    public ResponseEntity<List<FlightDto>> getAllFlights() {

        return ResponseEntity.ok(
                flightService.getAllFlights()
        );
    }


    @GetMapping("/available")
    @Operation(
            summary = "Get available flights",
            description = "Returns flights that are currently available for booking"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Available flights retrieved successfully"
            )
    })
    public ResponseEntity<List<FlightDto>> getAvailableFlights() {

        return ResponseEntity.ok(
                flightService.getAvailableFlights()
        );
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Get flight by ID",
            description = "Returns details for a specific flight"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Flight found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight not found"
            )
    })
    public ResponseEntity<FlightDto> getFlight(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                flightService.getFlightById(id)
        );
    }
}
