package se.lexicon.flightbooking_api.exception;


import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;



@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleFlightNotFound(
            FlightNotFoundException ex,
            HttpServletRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                404,
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );

    }



    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleBookingNotFound(
            BookingNotFoundException ex,
            HttpServletRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                404,
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );

    }



    @ExceptionHandler(SeatUnavailableException.class)
    public ResponseEntity<ErrorResponse>
    handleSeatUnavailable(
            SeatUnavailableException ex,
            HttpServletRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                400,
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );

    }



    @ExceptionHandler(PassengerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse>
    handlePassengerExists(
            PassengerAlreadyExistsException ex,
            HttpServletRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                409,
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );

    }



    @ExceptionHandler(InvalidTripException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidTrip(
            InvalidTripException ex,
            HttpServletRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                400,
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );

    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                500,
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );

    }

}