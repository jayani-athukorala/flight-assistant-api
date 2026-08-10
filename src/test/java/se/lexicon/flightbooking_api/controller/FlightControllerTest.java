package se.lexicon.flightbooking_api.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;


import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.entity.FlightSeat;

import se.lexicon.flightbooking_api.entity.enums.FlightStatus;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;


import se.lexicon.flightbooking_api.repository.FlightRepository;


import java.time.LocalDateTime;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlightControllerTest {



    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private FlightRepository flightRepository;



    @BeforeEach
    void setup(){

        flightRepository.deleteAll();

    }





    @Test
    void getAllFlightsReturnsAllFlights()
            throws Exception {


        flightRepository.save(
                createFlight(
                        "FL001",
                        "London"
                )
        );


        flightRepository.save(
                createFlight(
                        "FL002",
                        "Paris"
                )
        );



        mockMvc.perform(
                        get("/api/flights")
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$[0].flightNumber")
                                .exists()
                );

    }





    @Test
    void getAvailableFlightsReturnsFlightsWithSeats()
            throws Exception {



        flightRepository.save(
                createFlight(
                        "FL001",
                        "London"
                )
        );



        mockMvc.perform(
                        get("/api/flights/available")
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                );

    }





    private Flight createFlight(
            String flightNumber,
            String destination
    ){


        Flight flight =
                Flight.builder()

                        .flightNumber(
                                flightNumber
                        )

                        .origin(
                                "Stockholm"
                        )

                        .destination(
                                destination
                        )

                        .departureTime(
                                LocalDateTime.now()
                                        .plusDays(5)
                        )

                        .arrivalTime(
                                LocalDateTime.now()
                                        .plusDays(5)
                                        .plusHours(2)
                        )

                        .status(
                                FlightStatus.AVAILABLE
                        )

                        .build();



        FlightSeat seat =
                FlightSeat.builder()

                        .seatNumber(
                                "E1"
                        )

                        .seatClass(
                                SeatClass.ECONOMY
                        )

                        .price(
                                199.99
                        )

                        .flight(
                                flight
                        )

                        .build();



        flight.getSeats()
                .add(seat);



        return flight;

    }


}