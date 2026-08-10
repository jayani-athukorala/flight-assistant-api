package se.lexicon.flightbooking_api.exception;


public class FlightNotFoundException extends RuntimeException {


    public FlightNotFoundException(Long id){

        super("Flight with id " + id + " was not found");

    }


    public FlightNotFoundException(String flightNumber){

        super("Flight with number "
                + flightNumber
                + " was not found");

    }

}
