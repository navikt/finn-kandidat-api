package no.nav.finnkandidatapi.kandidat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.time.LocalDateTime;

import static no.nav.finnkandidatapi.TestData.*;

public class KandidatTest {

    @Test
    public void opprettKandidat__skal_opprette_en_kandidat_med_riktige_felt() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        LocalDateTime nå = now();

        Kandidat opprettetKandidat = Kandidat.opprettKandidat(kandidatDto, veileder, nå);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(opprettetKandidat.getId()).isEqualTo(kandidat.getId());
            softly.assertThat(opprettetKandidat.getFnr()).isEqualTo(kandidat.getFnr());
            softly.assertThat(opprettetKandidat.getAktørId()).isEqualTo(kandidat.getAktørId());
            softly.assertThat(opprettetKandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
            softly.assertThat(opprettetKandidat.getSistEndretAvVeileder()).isEqualTo(nå);
            softly.assertThat(opprettetKandidat.getArbeidstid()).isEqualTo(kandidatDto.getArbeidstid());
            softly.assertThat(opprettetKandidat.getFysisk()).isEqualTo(kandidatDto.getFysisk());
            softly.assertThat(opprettetKandidat.getArbeidshverdagen()).isEqualTo(kandidatDto.getArbeidshverdagen());
            softly.assertThat(opprettetKandidat.getUtfordringerMedNorsk()).isEqualTo(kandidatDto.getUtfordringerMedNorsk());
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
            softly.assertThat(endretKandidat.getSistEndretAvVeileder()).isEqualTo(nå);
            softly.assertThat(endretKandidat.getArbeidstid()).isEqualTo(kandidatDto.getArbeidstid());
            softly.assertThat(endretKandidat.getFysisk()).isEqualTo(kandidatDto.getFysisk());
            softly.assertThat(endretKandidat.getArbeidshverdagen()).isEqualTo(kandidatDto.getArbeidshverdagen());
            softly.assertThat(endretKandidat.getUtfordringerMedNorsk()).isEqualTo(kandidatDto.getUtfordringerMedNorsk());
        });
    }

}
