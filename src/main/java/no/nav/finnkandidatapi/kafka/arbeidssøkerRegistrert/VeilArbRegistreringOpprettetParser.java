package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Slf4j
public class VeilArbRegistreringOpprettetParser {

    public static LocalDateTime parseTidspunkt(String tidspunkt) {
        try {
            return ZonedDateTime.parse(tidspunkt).toLocalDateTime();
        } catch (Exception e) {
            log.warn("Klarte ikke å parse tidspunket fra veilarbreg, bruker now() istedet" );
            return LocalDateTime.now();
        }
    }
}
