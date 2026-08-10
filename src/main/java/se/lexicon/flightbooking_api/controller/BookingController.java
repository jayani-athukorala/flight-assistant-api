package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(
        name = "Flight Booking",
        description = "APIs for booking and managing flights"
)
public class BookingController {

    private final BookingService bookingService;

    // -------------------------------------------------
    // CREATE BOOKING
    // -------------------------------------------------

    @PostMapping("/{flightId}/book")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Book a flight",
            description = "Creates a booking for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Booking created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid booking request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight not found"
            )
    })
    public ResponseEntity<BookingResponseDto> bookFlight(
            @PathVariable Long flightId,
            @Valid @RequestBody BookingRequestDto request
    ) {

        BookingRequestDto updatedRequest =
                new BookingRequestDto(
                        flightId,
                        request.returnFlightId(),
                        request.tripType(),
                        request.seatClass(),
                        request.passengers()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bookingService.createBooking(updatedRequest)
                );
    }

    // -------------------------------------------------
    // MY BOOKINGS
    // -------------------------------------------------

    @GetMapping("/bookings/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get my bookings",
            description = "Returns bookings belonging to the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bookings retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    public ResponseEntity<List<BookingResponseDto>> getMyBookings() {

        return ResponseEntity.ok(
                bookingService.getMyBookings()
        );
    }

    // -------------------------------------------------
    // CANCEL BOOKING
    // -------------------------------------------------

    @DeleteMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Cancel my booking",
            description = "Cancels a booking belonging to the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Booking cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Booking does not belong to authenticated user"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found"
            )
    })
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId
    ) {

        bookingService.cancelBooking(bookingId);

        return ResponseEntity.noContent().build();
    }
}