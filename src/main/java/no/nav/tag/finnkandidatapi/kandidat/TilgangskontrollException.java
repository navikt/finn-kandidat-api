package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TilgangskontrollException extends RuntimeException {

    public TilgangskontrollException(String message) {
        super(message);
    }

    public TilgangskontrollException(String message, Throwable cause) {
        super(message, cause);
    }
}
