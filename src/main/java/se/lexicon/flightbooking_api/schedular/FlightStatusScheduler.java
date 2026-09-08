package se.lexicon.flightbooking_api.schedular;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.service.FlightService;

@Component
@RequiredArgsConstructor
public class FlightStatusScheduler {

    private final FlightService flightService;

    @Scheduled(fixedRate = 60_000)
    public void updateFlightStatuses() {
        flightService.updateFlightStatuses();
    }
}
