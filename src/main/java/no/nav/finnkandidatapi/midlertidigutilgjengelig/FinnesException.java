package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.CONFLICT)
public class FinnesException extends RuntimeException {
    public FinnesException() {
        super();
    }

    public FinnesException(String s) {
        super(s);
    }

    public FinnesException(String s, HttpClientErrorException e) {
        super(s, e);
    }
}
