package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.service.FlightSeatService;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(
        name = "Flight Seats",
        description = "Flight seat availability and seat classes"
)
public class FlightSeatController {

    private final FlightSeatService flightSeatService;


    @GetMapping("/{flightId}/seats")
    @Operation(
            summary = "Get available seats",
            description = "Returns all available seats for a flight"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Seats retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight not found"
            )
    })
    public ResponseEntity<List<FlightSeatDto>> getSeats(
            @PathVariable Long flightId
    ) {

        return ResponseEntity.ok(
                flightSeatService.getAvailableSeats(flightId)
        );
    }


    @GetMapping("/{flightId}/seats/class")
    @Operation(
            summary = "Get available seats by class",
            description = "Returns available seats filtered by seat class"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Seats retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight not found"
            )
    })
    public ResponseEntity<List<FlightSeatDto>> getSeatsByClass(
            @PathVariable Long flightId,
            @RequestParam SeatClass seatClass
    ) {

        return ResponseEntity.ok(
                flightSeatService.getAvailableSeatsByClass(
                        flightId,
                        seatClass
                )
        );
    }
}