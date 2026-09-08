package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.flight.CreateFlightDto;
import jakarta.validation.Valid;
import se.lexicon.flightbooking_api.service.FlightService;

import java.util.List;
import java.time.LocalDate;

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
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create flight",
            description = "Creates a scheduled flight and automatically generates its seats and prices"
    )
    public ResponseEntity<FlightDto> createFlight(
            @Valid @RequestBody CreateFlightDto request
    ) {
        return ResponseEntity.ok(flightService.createFlight(request));
    }


//     @GetMapping("/available")
//     @Operation(
//             summary = "Get available flights",
//             description = "Returns flights that are currently available for booking"
//     )
//     @ApiResponses(value = {
//             @ApiResponse(
//                     responseCode = "200",
//                     description = "Available flights retrieved successfully"
//             )
//     })
//     public ResponseEntity<List<FlightDto>> getAvailableFlights() {

//         return ResponseEntity.ok(
//                 flightService.getAvailableFlights()
//         );
//     }

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
    public ResponseEntity<List<FlightDto>> getAvailableFlights(
            @RequestParam Long originId,
            @RequestParam Long destinationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                date == null
                        ? flightService.getAvailableFlights(
                        originId,
                        destinationId
                )
                        : flightService.getAvailableFlights(
                        originId,
                        destinationId,
                        date
                )
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
