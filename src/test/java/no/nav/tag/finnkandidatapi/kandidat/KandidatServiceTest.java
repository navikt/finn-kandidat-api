package no.nav.tag.finnkandidatapi.kandidat;

import org.junit.Test;

import java.time.LocalDateTime;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;

public class KandidatServiceTest {

    @Test
    public void lagreKandidat__skal_endre_sistEndretAv_med_innlogget_veileder() {
        KandidatService kandidatService = new KandidatService();
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();

        kandidatService.oppdaterKandidat(kandidat, veileder);

        assertThat(kandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
        assertThat(kandidat.getSistEndret()).isEqualToIgnoringSeconds(LocalDateTime.now());
    }
}
