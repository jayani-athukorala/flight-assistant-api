package se.lexicon.flightbooking_api.assistant.dto;

import java.util.List;

/** Structured UI instructions for collecting passenger details in React. */
public record PassengerFormSpec(
        Long outboundFlightId,
        Long returnFlightId,
        List<PassengerSeatPair> seatPairs
) {
    public PassengerFormSpec {
        seatPairs = seatPairs == null ? List.of() : List.copyOf(seatPairs);
    }

    public record PassengerSeatPair(
            Long outboundSeatId,
            String outboundSeatNumber,
            Long returnSeatId,
            String returnSeatNumber
    ) {
    }
}
