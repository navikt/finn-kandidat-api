package no.nav.tag.finnkandidatapi.tilgangskontroll;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TilgangskontrollException extends RuntimeException {

    public TilgangskontrollException() {
        super();
    }

    public TilgangskontrollException(String message) {
        super(message);
    }

    public TilgangskontrollException(String message, Throwable cause) {
        super(message, cause);
    }
}
