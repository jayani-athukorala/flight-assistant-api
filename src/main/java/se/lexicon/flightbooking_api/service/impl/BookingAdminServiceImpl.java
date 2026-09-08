package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.mapper.BookingMapper;
import se.lexicon.flightbooking_api.repository.BookingRepository;
import se.lexicon.flightbooking_api.service.BookingAdminService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingAdminServiceImpl implements BookingAdminService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> searchBookings(
            BookingStatus status,
            String createdByEmail,
            String bookingReference,
            String passportNumber,
            Long flightId,
            LocalDate from,
            LocalDate to,
            Boolean archived,
            Pageable pageable
    ) {
        Specification<Booking> specification =
                Specification.unrestricted();

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (createdByEmail != null && !createdByEmail.isBlank()) {
            String createdByPattern =
                    "%" + createdByEmail.trim().toLowerCase() + "%";

            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(
                                            root.get("createdBy")
                                                    .get("email")
                                    ),
                                    createdByPattern
                            )
            );
        }

        if (bookingReference != null && !bookingReference.isBlank()) {
            String referencePattern =
                    "%" + bookingReference.trim().toLowerCase() + "%";

            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("bookingReference")),
                                    referencePattern
                            )
            );
        }

        if (passportNumber != null &&
                !passportNumber.isBlank()) {

            String passportPattern =
                    "%" + passportNumber
                            .trim()
                            .toLowerCase() + "%";

            specification = specification.and(
                    (root, query, cb) -> {

                        query.distinct(true);

                        return cb.like(
                                cb.lower(
                                        root.join("passengers")
                                                .get("passportNumber")
                                ),
                                passportPattern
                        );
                    }
            );
        }

        if (flightId != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.or(
                                    cb.equal(
                                            root.get("outboundFlight")
                                                    .get("id"),
                                            flightId
                                    ),
                                    cb.equal(
                                            root.get("returnFlight")
                                                    .get("id"),
                                            flightId
                                    )
                            )
            );
        }

        if (from != null) {
            LocalDateTime fromDate =
                    from.atStartOfDay();

            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("bookingDate"),
                                    fromDate
                            )
            );
        }

        if (to != null) {
            LocalDateTime exclusiveEnd =
                    to.plusDays(1).atStartOfDay();

            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThan(
                                    root.get("bookingDate"),
                                    exclusiveEnd
                            )
            );
        }

        if (archived != null) {
            specification = specification.and(
                    archived
                            ? (root, query, cb) ->
                            cb.isNotNull(
                                    root.get("archivedAt")
                            )
                            : (root, query, cb) ->
                            cb.isNull(
                                    root.get("archivedAt")
                            )
            );
        }

        return bookingRepository
                .findAll(specification, pageable)
                .map(bookingMapper::toDto);
    }
}
