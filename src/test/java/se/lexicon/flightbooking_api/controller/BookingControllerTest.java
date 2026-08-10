package se.lexicon.flightbooking_api.controller;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;



import se.lexicon.flightbooking_api.entity.*;

import se.lexicon.flightbooking_api.entity.enums.*;


import se.lexicon.flightbooking_api.repository.*;


import java.time.LocalDateTime;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;




@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest {



    @Autowired
    private MockMvc mockMvc;



    @Autowired
    private FlightRepository flightRepository;



    @Autowired
    private BookingRepository bookingRepository;




    @BeforeEach
    void setup(){


        bookingRepository.deleteAll();

        flightRepository.deleteAll();

    }






    @Test
    void bookFlightCreatesBooking()
            throws Exception {



        Flight flight =
                flightRepository.save(
                        createFlight()
                );




        mockMvc.perform(

                        post(
                                "/api/flights/{id}/book",
                                flight.getId()
                        )

                                .contentType(
                                        "application/json"
                                )


                                .content("""
                {
                  "tripType":"ONE_WAY",
                  "seatClass":"ECONOMY",
                  "passengers":[
                    {
                      "firstName":"Jane",
                      "lastName":"Doe",
                      "passportNumber":"P123456",
                      "email":"jane@test.com"
                    }
                  ]
                }
                """)

                )


                .andExpect(
                        status().isCreated()
                )


                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                );

    }







    @Test
    void getBookingsByEmailReturnsBooking()
            throws Exception {



        Booking booking =
                createBooking();



        bookingRepository.save(
                booking
        );



        mockMvc.perform(

                        get("/api/flights/bookings")
                                .param(
                                        "email",
                                        "jane@test.com"
                                )

                )


                .andExpect(
                        status().isOk()
                )


                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                );

    }








    private Flight createFlight(){


        Flight flight =
                Flight.builder()

                        .flightNumber(
                                "FL001"
                        )

                        .origin(
                                "Stockholm"
                        )

                        .destination(
                                "London"
                        )

                        .departureTime(
                                LocalDateTime.now()
                        )

                        .arrivalTime(
                                LocalDateTime.now()
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







    private Booking createBooking(){


        Flight flight =
                createFlight();



        Passenger passenger =
                Passenger.builder()

                        .firstName(
                                "Jane"
                        )

                        .lastName(
                                "Doe"
                        )

                        .passportNumber(
                                "P123456"
                        )

                        .email(
                                "jane@test.com"
                        )

                        .build();



        Booking booking =
                Booking.builder()

                        .bookingReference(
                                "FB-12345"
                        )

                        .bookingDate(
                                LocalDateTime.now()
                        )

                        .status(
                                BookingStatus.CONFIRMED
                        )

                        .tripType(
                                TripType.ONE_WAY
                        )

                        .outboundFlight(
                                flight
                        )

                        .totalPrice(
                                199.99
                        )

                        .build();



//        passenger.setBooking(
//                booking
//        );


        booking.getPassengers()
                .add(passenger);



        return booking;


    }


}