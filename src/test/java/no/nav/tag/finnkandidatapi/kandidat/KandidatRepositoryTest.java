package no.nav.tag.finnkandidatapi.kandidat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static no.nav.tag.finnkandidatapi.TestData.*;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.ANNET;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.FADDER;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ARBEIDSSTILLING;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KandidatRepositoryTest {

    @Autowired
    private KandidatRepository repository;

    @Before
    public void setUp() {
        repository.slettAlleKandidater();
    }

    @Test
    public void skal_kunne_lagre_og_hente_kandidat() {
        Kandidat behovTilLagring = enKandidat();

        Integer lagretId = repository.lagreKandidat(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null_og_tomme_set() {
        Kandidat behovTilLagring = enKandidatMedNullOgTommeSet();

        Integer lagretId = repository.lagreKandidat(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null() {
        Kandidat behovTilLagring = enKandidatMedBareNull();

        Integer lagretId = repository.lagreKandidat(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(enKandidatMedNullOgTommeSet(), "id");
    }

    @Test
    public void hentNyesteKandidat__skal_returnere_siste_registrerte_kandidat() {
        Kandidat kandidat1 = enKandidat();
        kandidat1.setFysiskeBehov(Set.of(ARBEIDSSTILLING));
        Kandidat kandidat2 = enKandidat();
        kandidat2.setArbeidsmiljøBehov(Set.of(FADDER, ANNET));

        repository.lagreKandidat(kandidat1);
        repository.lagreKandidat(kandidat2);
        Kandidat sisteKandidat = repository.hentNyesteKandidat(kandidat1.getFnr()).get();

        assertThat(sisteKandidat).isEqualToIgnoringGivenFields(kandidat2, "id");
    }

    @Test
    public void hentNyesteKandidat__skal_håndtere_henting_av_ikke_eksisterende_kandidat() {
        boolean kandidatEksisterer = repository.hentNyesteKandidat("finnes ikke").isPresent();
        assertThat(kandidatEksisterer).isFalse();
    }

    @Test
    public void hentKandidat__skal_håndtere_henting_av_ikke_eksisterende_kandidat() {
        boolean kandidatEksisterer = repository.hentKandidat(100).isPresent();
        assertThat(kandidatEksisterer).isFalse();
    }

    @Test
    public void hentKandidater__skal_returnere_lagrede_kandidater() {
        Kandidat kandidat1 = enKandidat();
        kandidat1.setFnr("01234567890");
        Kandidat kandidat2 = enKandidat();
        kandidat2.setFnr("12345678901");
        Kandidat kandidat3 = enKandidat();
        kandidat3.setFnr("23456789012");
        repository.lagreKandidat(kandidat1);
        repository.lagreKandidat(kandidat2);
        repository.lagreKandidat(kandidat3);

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.size()).isEqualTo(3);
        assertThat(kandidater.get(0)).isEqualToIgnoringGivenFields(kandidat1, "id");
        assertThat(kandidater.get(1)).isEqualToIgnoringGivenFields(kandidat2, "id");
        assertThat(kandidater.get(2)).isEqualToIgnoringGivenFields(kandidat3, "id");
    }

    @Test
    public void hentKandidater__skal_returnere_siste_kandidat_etter_lagret_flere_kandidater_med_samme_fnr() {
        Kandidat kandidat = kandidatBuilder()
                .fnr("01234567890")
                .sistEndret(LocalDateTime.now())
                .build();
        Kandidat nyereKandidat = kandidatBuilder()
                .fnr("01234567890")
                .sistEndret(LocalDateTime.now().plusMinutes(1))
                .build();
        Kandidat sisteKandidat = kandidatBuilder()
                .fnr("01234567890")
                .sistEndret(LocalDateTime.now().plusMinutes(2))
                .build();

        repository.lagreKandidat(kandidat);
        repository.lagreKandidat(nyereKandidat);
        repository.lagreKandidat(sisteKandidat);

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.size()).isEqualTo(1);
        assertThat(kandidater.get(0)).isEqualToIgnoringGivenFields(sisteKandidat, "id");
    }

    @Test
    public void hentKandidater__skal_returnere_kandidater_sortert_på_sist_endret_tidspunkt() {
        Kandidat kandidat1 = enKandidat();
        kandidat1.setSistEndret(LocalDateTime.now().plusMinutes(1));
        kandidat1.setFnr("1234567890");

        Kandidat kandidat2 = enKandidat();
        kandidat2.setSistEndret(LocalDateTime.now());
        kandidat2.setFnr("2345678901");

        repository.lagreKandidat(kandidat1);
        repository.lagreKandidat(kandidat2);

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.get(0)).isEqualToIgnoringGivenFields(kandidat2, "id");
        assertThat(kandidater.get(1)).isEqualToIgnoringGivenFields(kandidat1, "id");
    }

    @Test
    public void hentKandidater__skal_returnere_tom_liste_hvis_ingen_kandidater() {
        List<Kandidat> kandidater = repository.hentKandidater();
        assertThat(kandidater).isEmpty();
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_flere_ganger() {
        Kandidat behovTilLagring1 = enKandidat();
        Integer lagretId1 = repository.lagreKandidat(behovTilLagring1);

        Kandidat behovTilLagring2 = enKandidat();
        Integer lagretId2 = repository.lagreKandidat(behovTilLagring2);

        Kandidat uthentetBehov1 = repository.hentKandidat(lagretId1).get();
        Kandidat uthentetBehov2 = repository.hentKandidat(lagretId2).get();

        assertThat(uthentetBehov1.getId()).isEqualTo(1);
        assertThat(uthentetBehov1).isEqualToIgnoringGivenFields(behovTilLagring1, "id");

        assertThat(uthentetBehov2.getId()).isEqualTo(2);
        assertThat(uthentetBehov2).isEqualToIgnoringGivenFields(behovTilLagring2, "id");
    }
}
