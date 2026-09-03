package se.lexicon.flightbooking_api.assistant.dto;

public enum AssistantResponseType {
    TEXT,
    FLIGHT_RESULTS,
    CONFIRMATION_REQUIRED,
    BOOKING_COMPLETED,
    CANCELLATION_COMPLETED,
    ERROR
}