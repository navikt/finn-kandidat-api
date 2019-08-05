package no.nav.tag.finnkandidatapi.logging;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Protected
@RestController
@RequestMapping("/logging")
public class LoggingController {

    @PostMapping
    public void logging(@RequestBody LoggEvent loggEvent) {
        log.info("event={}, felter={}", loggEvent.getEventnavn(), loggEvent.getFelter());
    }
}
