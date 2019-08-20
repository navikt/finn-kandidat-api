package no.nav.tag.finnkandidatapi.logging;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static no.nav.tag.finnkandidatapi.TestData.enLoggEventMedIntTag;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LoggingControllerTest {

    private LoggingController loggingController;

    // TODO: Fjern
    @Mock
    private MeterRegistry meterRegistry;

    @Before
    public void setUp() {
       loggingController = new LoggingController(meterRegistry);
    }

    // TODO: Ta tilbake
//    @Test
//    public void sendEvent__skal_returnere_ok_ved_ok_event() {
//        ResponseEntity respons = loggingController.sendEvent(enLoggEvent());
//        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }

    @Test
    public void sendEvent__skal_returnere_bad_request_hvis_tags_inneholder_noe_annet_enn_strings() {
        ResponseEntity respons = loggingController.sendEvent(enLoggEventMedIntTag());
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
