package no.nav.finnkandidatapi.tilgangskontroll;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
public class IkkeIPilotException extends RuntimeException {

    public IkkeIPilotException(String message) {
        super(message);
    }
}
