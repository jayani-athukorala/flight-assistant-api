package se.lexicon.flightbooking_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.service.BookingAdminService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BookingAdminController {

    private final BookingAdminService bookingAdminService;

    @GetMapping
    public ResponseEntity<Page<BookingResponseDto>> searchBookings(
            @RequestParam(required = false)
            BookingStatus status,

            @RequestParam(required = false)
            String email,

            @RequestParam(required = false)
            String passportNumber,

            @RequestParam(required = false)
            Long flightId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(required = false)
            Boolean archived,

            @PageableDefault(
                    size = 20,
                    sort = "bookingDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                bookingAdminService.searchBookings(
                        status,
                        email,
                        passportNumber,
                        flightId,
                        from,
                        to,
                        archived,
                        pageable
                )
        );
    }
}
