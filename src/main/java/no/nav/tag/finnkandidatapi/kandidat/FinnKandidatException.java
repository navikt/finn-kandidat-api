package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FinnKandidatException extends RuntimeException {
}
