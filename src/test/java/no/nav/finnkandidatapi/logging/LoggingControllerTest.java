package no.nav.finnkandidatapi.logging;

import no.nav.finnkandidatapi.TestData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class LoggingControllerTest {

    private LoggingController loggingController;

    @Before
    public void setUp() {
       loggingController = new LoggingController();
    }

    @Test
    public void sendEvent__skal_returnere_ok_ved_ok_event() {
        ResponseEntity respons = loggingController.sendEvent(TestData.enLoggEvent());
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void sendEvent__skal_returnere_bad_request_hvis_tags_inneholder_noe_annet_enn_strings() {
        ResponseEntity respons = loggingController.sendEvent(TestData.enLoggEventMedIntTag());
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
