package no.nav.tag.finnkandidatapi.kandidat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.time.LocalDateTime;

import static no.nav.tag.finnkandidatapi.TestData.*;

public class KandidatTest {

    @Test
    public void endreKandidat__skal_endre_behovene_til_en_kandidat() {
        Kandidat kandidat = enKandidat();
        Kandidatendring kandidatendring = enKandidatendring();
        Veileder veileder = enVeileder();
        LocalDateTime nå = LocalDateTime.now();

        Kandidat endretKandidat = Kandidat.endreKandidat(kandidat, kandidatendring, veileder, nå);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(endretKandidat.getId()).isEqualTo(kandidat.getId());
            softly.assertThat(endretKandidat.getFnr()).isEqualTo(kandidat.getFnr());
            softly.assertThat(endretKandidat.getAktørId()).isEqualTo(kandidat.getAktørId());
            softly.assertThat(endretKandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
            softly.assertThat(endretKandidat.getSistEndret()).isEqualTo(nå);
            softly.assertThat(endretKandidat.getArbeidstidBehov()).isEqualTo(kandidatendring.getArbeidstidBehov());
            softly.assertThat(endretKandidat.getFysiskeBehov()).isEqualTo(kandidatendring.getFysiskeBehov());
            softly.assertThat(endretKandidat.getArbeidsmiljøBehov()).isEqualTo(kandidatendring.getArbeidsmiljøBehov());
            softly.assertThat(endretKandidat.getGrunnleggendeBehov()).isEqualTo(kandidatendring.getGrunnleggendeBehov());
            softly.assertThat(endretKandidat.getNavKontor()).isEqualTo(kandidat.getNavKontor());
        });
    }

}
