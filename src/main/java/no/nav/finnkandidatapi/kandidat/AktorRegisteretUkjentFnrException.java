package no.nav.finnkandidatapi.kandidat;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AktorRegisteretUkjentFnrException extends FinnKandidatException {
    public AktorRegisteretUkjentFnrException() {
        super();
    }

    public AktorRegisteretUkjentFnrException(String s) {
        super(s);
    }

    public AktorRegisteretUkjentFnrException(String s, HttpClientErrorException e) {
        super(s, e);
    }
}
