package no.nav.finnkandidatapi.kafka;

import no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert.VeilArbRegistreringOpprettetParser;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class VeilArbRegistreringOpprettetParserTest {

    @Test
    public void skal_parse_tidspunkt_fra_veilarbreg_topic_korrekt() {
        LocalDateTime localDateTime = VeilArbRegistreringOpprettetParser.parseTidspunkt("2018-03-31T14:15:34.708694+02:00[Europe/Oslo]" );
        assertThat(localDateTime.getYear()).isEqualTo(2018);
    }

}
