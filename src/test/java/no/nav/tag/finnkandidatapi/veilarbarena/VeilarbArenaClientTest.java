package no.nav.tag.finnkandidatapi.veilarbarena;

import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.sts.STSClient;
import no.nav.tag.finnkandidatapi.sts.STSToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.personinfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VeilarbArenaClientTest {
    private VeilarbArenaClient veilarbArenaClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        veilarbArenaClient = new VeilarbArenaClient(restTemplate, "https://www.eksempel.no");
    }

    @Test
    public void hentPersonInfo__skal_hente_personinfo() {
        Kandidat kandidat = enKandidat();
        Personinfo personinfo = personinfo();

        when(restTemplate.getForEntity(anyString(), eq(Personinfo.class)))
                .thenReturn(new ResponseEntity<>(personinfo, HttpStatus.OK));

        assertThat(veilarbArenaClient.hentPersoninfo(kandidat.getFnr())).isEqualTo(personinfo);
    }
}
