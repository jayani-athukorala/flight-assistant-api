package se.lexicon.flightbooking_api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.FlightSeat;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void getAllFlightsReturnsAllFlights()
            throws Exception {

        Airport stockholm = createAirport(
                "ARN",
                "Stockholm Arlanda Airport",
                "Stockholm",
                "Sweden"
        );

        Airport london = createAirport(
                "LHR",
                "Heathrow Airport",
                "London",
                "United Kingdom"
        );

        Airport paris = createAirport(
                "CDG",
                "Charles de Gaulle Airport",
                "Paris",
                "France"
        );

        createFlight(
                "SK001",
                stockholm,
                london,
                FlightStatus.SCHEDULED
        );

        createFlight(
                "SK002",
                stockholm,
                paris,
                FlightStatus.SCHEDULED
        );

        mockMvc.perform(
                        get("/api/flights")
                                .with(
                                        user("admin@test.com")
                                                .roles("ADMIN")
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(2)
                )
                .andExpect(
                        jsonPath("$[0].flightNumber").exists()
                )
                .andExpect(
                        jsonPath("$[0].origin.code").value("ARN")
                );
    }

    @Test
    void getAvailableFlightsFiltersByAirportIds()
            throws Exception {

        Airport stockholm = createAirport(
                "ARN",
                "Stockholm Arlanda Airport",
                "Stockholm",
                "Sweden"
        );

        Airport london = createAirport(
                "LHR",
                "Heathrow Airport",
                "London",
                "United Kingdom"
        );

        Airport paris = createAirport(
                "CDG",
                "Charles de Gaulle Airport",
                "Paris",
                "France"
        );

        createFlight(
                "SK001",
                stockholm,
                london,
                FlightStatus.SCHEDULED
        );

        createFlight(
                "SK002",
                stockholm,
                paris,
                FlightStatus.SCHEDULED
        );

        createFlight(
                "SK003",
                stockholm,
                london,
                FlightStatus.CANCELLED
        );

        mockMvc.perform(
                        get("/api/flights/available")
                                .param(
                                        "originId",
                                        stockholm.getId().toString()
                                )
                                .param(
                                        "destinationId",
                                        london.getId().toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(1)
                )
                .andExpect(
                        jsonPath("$[0].flightNumber")
                                .value("SK001")
                )
                .andExpect(
                        jsonPath("$[0].origin.id")
                                .value(stockholm.getId())
                )
                .andExpect(
                        jsonPath("$[0].destination.id")
                                .value(london.getId())
                )
                .andExpect(
                        jsonPath("$[0].startingPrice")
                                .value(199.99)
                );
    }

    @Test
    void availableFlightsRejectsSameAirport()
            throws Exception {

        Airport stockholm = createAirport(
                "ARN",
                "Stockholm Arlanda Airport",
                "Stockholm",
                "Sweden"
        );

        mockMvc.perform(
                        get("/api/flights/available")
                                .param(
                                        "originId",
                                        stockholm.getId().toString()
                                )
                                .param(
                                        "destinationId",
                                        stockholm.getId().toString()
                                )
                )
                .andExpect(status().isBadRequest());
    }

    private Airport createAirport(
            String code,
            String name,
            String city,
            String country
    ) {
        return airportRepository.save(
                Airport.builder()
                        .code(code)
                        .name(name)
                        .city(city)
                        .country(country)
                        .build()
        );
    }

    private Flight createFlight(
            String flightNumber,
            Airport origin,
            Airport destination,
            FlightStatus status
    ) {
        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .airline("SAS")
                .origin(origin)
                .destination(destination)
                .departureTime(
                        LocalDateTime.now().plusDays(5)
                )
                .arrivalTime(
                        LocalDateTime.now()
                                .plusDays(5)
                                .plusHours(2)
                )
                .status(status)
                .build();

        flight.addSeat(
                FlightSeat.builder()
                        .seatNumber(
                                flightNumber + "-12A"
                        )
                        .seatClass(SeatClass.ECONOMY)
                        .price(
                                new BigDecimal("199.99")
                        )
                        .build()
        );

        return flightRepository.saveAndFlush(flight);
    }
}