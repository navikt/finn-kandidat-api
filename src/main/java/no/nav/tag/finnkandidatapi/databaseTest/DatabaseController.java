package no.nav.tag.finnkandidatapi.databaseTest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {

    private final FinnKandidatRepository repository;

    public DatabaseController(FinnKandidatRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/db-test")
    public String healthcheck() {
        return repository.healthcheck();
    }
}
