package se.lexicon.flightbooking_api.assistant.dto;

public enum AssistantResponseType {
    TEXT,
    AIRPORT_RESULTS,
    FLIGHT_RESULTS,
    SEAT_RESULTS,
    BOOKING_RESULTS,
    AUTHENTICATION_REQUIRED,
    CONFIRMATION_REQUIRED,
    BOOKING_COMPLETED,
    CANCELLATION_COMPLETED,
    ERROR
}
