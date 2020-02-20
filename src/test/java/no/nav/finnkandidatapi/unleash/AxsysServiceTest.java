package no.nav.finnkandidatapi.unleash;

import no.nav.finnkandidatapi.unleash.enhet.AxsysService;
import no.nav.finnkandidatapi.unleash.enhet.NavEnhet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"local", "mock"})
@DirtiesContext
public class AxsysServiceTest {

    @Autowired
    private AxsysService axsysService;

    @Test
    public void hentEnheter__returnerer_riktige_enheter() {
        List<NavEnhet> enheter = axsysService.hentEnheterVeilederHarTilgangTil("X123456");
        assertThat(enheter).containsOnly(new NavEnhet("0213"), new NavEnhet("0315"));
    }

    @Test
    public void pilotEnheter__inneholder_hentetEnheter() {
        List<NavEnhet> enheter = axsysService.hentEnheterVeilederHarTilgangTil("X123456");
        List<NavEnhet> pilotEnheter = asList(new NavEnhet("0213"));
        assertThat(pilotEnheter).containsAnyElementsOf(enheter);
    }
}
