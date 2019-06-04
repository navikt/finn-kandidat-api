package no.nav.tag.finnkandidatapi;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.metrikker.MetrikkRegistrering;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.lidalia.slf4jtest.LoggingEvent.info;

@RunWith(MockitoJUnitRunner.class)
public class MetrikkRegistreringTest {

    private MetrikkRegistrering metrikkRegistrering;

    @Mock
    private MeterRegistry meterRegistry;

    private TestLogger logger = TestLoggerFactory.getTestLogger(MetrikkRegistrering.class);

    @Before
    public void setUp() {
        metrikkRegistrering = new MetrikkRegistrering(meterRegistry);
    }

    @Test
    public void kandidatOpprettet__skal_logge_event() {
        Kandidat kandidat = enKandidat();
        metrikkRegistrering.kandidatOpprettet(new KandidatOpprettet(kandidat));
        assertThat(logger.getLoggingEvents().get(0)).isEqualTo(info("event=kandidat.opprettet, id={}", kandidat.getId()));
    }

    @Test
    public void kandidatEndret__skal_logge_event() {
        Kandidat kandidat = enKandidat();
        metrikkRegistrering.kandidatEndret(new KandidatEndret(kandidat));
        assertThat(logger.getLoggingEvents().get(0)).isEqualTo(info("event=kandidat.endret, id={}", kandidat.getId()));
    }

    @After
    public void tearDown() {
        TestLoggerFactory.clear();
    }
}
