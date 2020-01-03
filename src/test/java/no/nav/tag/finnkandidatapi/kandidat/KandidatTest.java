package no.nav.tag.finnkandidatapi.kandidat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.time.LocalDateTime;

import static no.nav.tag.finnkandidatapi.TestData.*;

public class KandidatTest {

    @Test
    public void opprettKandidat__skal_opprette_en_kandidat_med_riktige_felt() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        LocalDateTime nå = now();

        Kandidat opprettetKandidat = Kandidat.opprettKandidat(
                kandidat.getFnr(),
                kandidatDto,
                veileder,
                nå,
                etNavKontor()
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(opprettetKandidat.getId()).isEqualTo(kandidat.getId());
            softly.assertThat(opprettetKandidat.getFnr()).isEqualTo(kandidat.getFnr());
            softly.assertThat(opprettetKandidat.getAktørId()).isEqualTo(kandidat.getAktørId());
            softly.assertThat(opprettetKandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
            softly.assertThat(opprettetKandidat.getSistEndret()).isEqualTo(nå);
            softly.assertThat(opprettetKandidat.getArbeidstidBehov()).isEqualTo(kandidatDto.getArbeidstidBehov());
            softly.assertThat(opprettetKandidat.getFysiskeBehov()).isEqualTo(kandidatDto.getFysiskeBehov());
            softly.assertThat(opprettetKandidat.getArbeidsmiljøBehov()).isEqualTo(kandidatDto.getArbeidsmiljøBehov());
            softly.assertThat(opprettetKandidat.getGrunnleggendeBehov()).isEqualTo(kandidatDto.getGrunnleggendeBehov());
            softly.assertThat(opprettetKandidat.getNavKontor()).isEqualTo(kandidat.getNavKontor());
        });
    }

    @Test
    public void endreKandidat__skal_endre_behovene_til_en_kandidat() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        LocalDateTime nå = now();

        Kandidat endretKandidat = Kandidat.endreKandidat(kandidat, kandidatDto, veileder, nå);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(endretKandidat.getId()).isEqualTo(kandidat.getId());
            softly.assertThat(endretKandidat.getFnr()).isEqualTo(kandidat.getFnr());
            softly.assertThat(endretKandidat.getAktørId()).isEqualTo(kandidat.getAktørId());
            softly.assertThat(endretKandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
            softly.assertThat(endretKandidat.getSistEndret()).isEqualTo(nå);
            softly.assertThat(endretKandidat.getArbeidstidBehov()).isEqualTo(kandidatDto.getArbeidstidBehov());
            softly.assertThat(endretKandidat.getFysiskeBehov()).isEqualTo(kandidatDto.getFysiskeBehov());
            softly.assertThat(endretKandidat.getArbeidsmiljøBehov()).isEqualTo(kandidatDto.getArbeidsmiljøBehov());
            softly.assertThat(endretKandidat.getGrunnleggendeBehov()).isEqualTo(kandidatDto.getGrunnleggendeBehov());
            softly.assertThat(endretKandidat.getNavKontor()).isEqualTo(kandidat.getNavKontor());
        });
    }

}
