package se.lexicon.flightbooking_api.exception;


public class SeatUnavailableException
        extends RuntimeException {


    public SeatUnavailableException(String seat){

        super("Seat "
                + seat
                + " is not available");

    }


}