package se.lexicon.flightbooking_api.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.assistant.action.PendingActionStore;
import se.lexicon.flightbooking_api.assistant.dto.*;
import se.lexicon.flightbooking_api.assistant.exception.AssistantModelException;
import se.lexicon.flightbooking_api.assistant.gateway.AssistantModelGateway;
import se.lexicon.flightbooking_api.assistant.gateway.AssistantModelResult;
import se.lexicon.flightbooking_api.assistant.history.ChatMessage;
import se.lexicon.flightbooking_api.assistant.history.ChatRole;
import se.lexicon.flightbooking_api.assistant.history.InMemoryConversationStore;
import se.lexicon.flightbooking_api.assistant.tool.AssistantToolExecutor;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.seat.FlightSeatDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantServiceImpl implements AssistantService {

    private static final String TEMPORARY_ERROR_MESSAGE =
            "The assistant is temporarily unavailable. Please try again shortly.";

    private final InMemoryConversationStore conversationStore;
    private final AssistantModelGateway modelGateway;
    private final PendingActionStore pendingActionStore;
    private final AssistantToolExecutor toolExecutor;

    @Override
    public AssistantChatResponse chat(AssistantChatRequest request, String ownerKey) {
        UUID conversationId = request.conversationId() == null
                ? UUID.randomUUID()
                : request.conversationId();

        conversationStore.append(
                conversationId,
                ownerKey,
                new ChatMessage(ChatRole.USER, request.message()));

        List<ChatMessage> conversation = conversationStore.getMessages(
                conversationId,
                ownerKey);

        try {
            AssistantModelResult result = modelGateway.generateReply(conversation);
            PendingAssistantAction pendingAction = result.pendingAction() == null
                    ? null
                    : pendingActionStore.save(
                            conversationId,
                            ownerKey,
                            result.pendingAction());

            AssistantResponseType responseType = determineResponseType(
                    result,
                    pendingAction);
            appendAssistantMessage(conversationId, ownerKey, result.message());

            return response(
                    conversationId,
                    result.message(),
                    responseType,
                    result.airports(),
                    result.flights(),
                    result.availableSeats(),
                    result.bookings(),
                    result.passengerForm(),
                    pendingAction);
        } catch (AssistantModelException exception) {
            log.error(
                    "Assistant model request failed for conversation {}",
                    conversationId,
                    exception);
            appendAssistantMessage(conversationId, ownerKey, TEMPORARY_ERROR_MESSAGE);
            return response(
                    conversationId,
                    TEMPORARY_ERROR_MESSAGE,
                    AssistantResponseType.ERROR,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    null);
        }
    }

    @Override
    public AssistantChatResponse confirmAction(
            UUID conversationId,
            UUID actionId,
            String ownerKey
    ) {
        PendingActionStore.StoredPendingAction action =
                pendingActionStore.consume(conversationId, actionId, ownerKey);

        if (action.publicAction().type() == PendingActionType.CREATE_BOOKING) {
            BookingResponseDto booking =
                    toolExecutor.createBooking(action.bookingRequest());
            String message = "Booking " + booking.bookingReference()
                    + " was created successfully.";
            appendAssistantMessage(conversationId, ownerKey, message);
            return response(
                    conversationId,
                    message,
                    AssistantResponseType.BOOKING_COMPLETED,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(booking),
                    null,
                    null);
        }

        BookingResponseDto booking = toolExecutor.cancelBooking(action.bookingId());
        String message = "Booking " + booking.bookingReference()
                + " was cancelled successfully.";
        appendAssistantMessage(conversationId, ownerKey, message);
        return response(
                conversationId,
                message,
                AssistantResponseType.CANCELLATION_COMPLETED,
                List.of(),
                List.of(),
                List.of(),
                List.of(booking),
                null,
                null);
    }

    @Override
    public void rejectAction(UUID conversationId, UUID actionId, String ownerKey) {
        pendingActionStore.reject(conversationId, actionId, ownerKey);
        appendAssistantMessage(
                conversationId,
                ownerKey,
                "The pending action was cancelled without making changes.");
    }

    @Override
    public void clearConversation(UUID conversationId, String ownerKey) {
        pendingActionStore.clearConversation(conversationId, ownerKey);
        conversationStore.clear(conversationId, ownerKey);
    }

    private AssistantResponseType determineResponseType(
            AssistantModelResult result,
            PendingAssistantAction pendingAction
    ) {
        if (pendingAction != null) {
            return AssistantResponseType.CONFIRMATION_REQUIRED;
        }
        if (result.authenticationRequired()) {
            return AssistantResponseType.AUTHENTICATION_REQUIRED;
        }
        if (result.passengerForm() != null) {
            return AssistantResponseType.PASSENGER_DETAILS_REQUIRED;
        }
        if (!result.bookings().isEmpty()) {
            return AssistantResponseType.BOOKING_RESULTS;
        }
        if (!result.availableSeats().isEmpty()) {
            return AssistantResponseType.SEAT_RESULTS;
        }
        if (!result.flights().isEmpty()) {
            return AssistantResponseType.FLIGHT_RESULTS;
        }
        if (!result.airports().isEmpty()) {
            return AssistantResponseType.AIRPORT_RESULTS;
        }
        return AssistantResponseType.TEXT;
    }

    private AssistantChatResponse response(
            UUID conversationId,
            String message,
            AssistantResponseType type,
            List<AirportResponseDto> airports,
            List<FlightDto> flights,
            List<FlightSeatDto> seats,
            List<BookingResponseDto> bookings,
            PassengerFormSpec passengerForm,
            PendingAssistantAction pendingAction
    ) {
        return new AssistantChatResponse(
                conversationId,
                message,
                type,
                airports,
                flights,
                seats,
                bookings,
                passengerForm,
                pendingAction != null,
                pendingAction);
    }

    private void appendAssistantMessage(
            UUID conversationId,
            String ownerKey,
            String message
    ) {
        conversationStore.append(
                conversationId,
                ownerKey,
                new ChatMessage(ChatRole.ASSISTANT, message));
    }
}
