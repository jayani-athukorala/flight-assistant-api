package se.lexicon.flightbooking_api.exception;


public class PassengerAlreadyExistsException
        extends RuntimeException {


    public PassengerAlreadyExistsException(String passport){

        super("Passenger with passport number "
                + passport
                + " already exists");

    }


}