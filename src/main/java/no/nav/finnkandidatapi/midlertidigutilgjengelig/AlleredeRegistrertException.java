package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlleredeRegistrertException extends RuntimeException {
    public AlleredeRegistrertException() {
        super();
    }

    public AlleredeRegistrertException(String s) {
        super(s);
    }

    public AlleredeRegistrertException(String s, HttpClientErrorException e) {
        super(s, e);
    }
}
