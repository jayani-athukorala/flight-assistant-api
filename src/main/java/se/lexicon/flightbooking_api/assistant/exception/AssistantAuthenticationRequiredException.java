package se.lexicon.flightbooking_api.assistant.exception;

public class AssistantAuthenticationRequiredException
        extends RuntimeException {

    public AssistantAuthenticationRequiredException(String message) {
        super(message);
    }
}
