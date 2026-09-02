package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.booking.BookingRequestDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.service.BookingAdminService;
import se.lexicon.flightbooking_api.service.BookingService;

import java.time.LocalDate;
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
    private final BookingAdminService bookingAdminService;

    // -------------------------------------------------
    // CREATE BOOKING
    // -------------------------------------------------

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
                    description = "Flight or seat not found"
            )
    })
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto request
    ) {
        BookingResponseDto booking =
                bookingService.createBooking(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(booking);
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

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(
            @RequestParam(defaultValue = "false") boolean archived
    ) {
        return ResponseEntity.ok(
                bookingService.getMyBookings(archived)
        );
    }

    // -------------------------------------------------
    // CANCEL BOOKING
    // -------------------------------------------------

    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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

    @PatchMapping("/{bookingId}/archive")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> archiveBooking(
            @PathVariable Long bookingId
    ) {
        bookingService.archiveBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{bookingId}/restore")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> restoreBooking(
            @PathVariable Long bookingId
    ) {
        bookingService.restoreArchivedBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<BookingResponseDto> searchBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String passportNumber,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Boolean archived,
            Pageable pageable
    ) {
        return bookingAdminService.searchBookings(
                status,
                email,
                passportNumber,
                flightId,
                from,
                to,
                archived,
                pageable
        );
    }
}