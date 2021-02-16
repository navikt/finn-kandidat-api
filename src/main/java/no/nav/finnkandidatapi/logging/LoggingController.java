package no.nav.finnkandidatapi.logging;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Protected
@RestController
@RequestMapping("/events")
public class LoggingController {

    private final MetricsClient metricsClient;

    public LoggingController(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
    }

    @PostMapping
    public ResponseEntity sendEvent(@RequestBody LoggEvent loggEvent) {
        log.info(
                "Mottatt event fra frontend. name={}, tags={}, fields={}",
                loggEvent.getName(),
                loggEvent.getTags(),
                loggEvent.getFields()
        );
        if (tagsInneholderNoeAnnetEnnStrings(loggEvent.getTags())) {
            return ResponseEntity.badRequest().body("Tags kan kun inneholde strings");
        }

        Event event = new Event(loggEvent.getName());
        leggTilTags(loggEvent.getTags(), event);
        leggTilFields(loggEvent.getFields(), event);

        metricsClient.report(event);
        return ResponseEntity.ok().build();
    }

    private boolean tagsInneholderNoeAnnetEnnStrings(JSONObject tags) {
        return tags.values().stream().anyMatch(verdi -> !(verdi instanceof String));
    }

    private void leggTilTags(JSONObject tags, Event event) {
        tags.forEach((tag, verdi) -> event.addTagToReport(tag, (String) verdi));
    }

    private void leggTilFields(JSONObject fields, Event event) {
        fields.forEach(event::addFieldToReport);
    }
}
