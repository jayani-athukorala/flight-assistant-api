package se.lexicon.flightbooking_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.service.AirportService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;

    @Transactional(readOnly = true)
    public List<AirportResponseDto> search(String query, int requestedLimit) {
        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.length() < 2) {
            return List.of();
        }

        int limit = Math.max(1, Math.min(requestedLimit, 20));

        return airportRepository
                .search(normalizedQuery, PageRequest.of(0, limit))
                .stream()
                .map(airport -> new AirportResponseDto(
                        airport.getId(),
                        airport.getCode(),
                        airport.getName(),
                        airport.getCity(),
                        airport.getCountry()
                ))
                .toList();
    }
}
