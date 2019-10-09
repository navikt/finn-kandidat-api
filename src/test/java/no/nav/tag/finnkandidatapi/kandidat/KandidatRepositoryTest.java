package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.HarTilretteleggingsbehov;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static no.nav.tag.finnkandidatapi.TestData.*;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.ANNET;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.MENTOR;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ARBEIDSSTILLING;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
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
        kandidat2.setArbeidsmiljøBehov(Set.of(MENTOR, ANNET));

        repository.lagreKandidat(kandidat1);
        repository.lagreKandidat(kandidat2);
        Kandidat sisteKandidat = repository.hentNyesteKandidat(kandidat1.getAktørId()).get();

        assertThat(sisteKandidat).isEqualToIgnoringGivenFields(kandidat2, "id");
    }

    @Test
    public void hentNyesteKandidat__skal_ikke_returnere_slettede_kandidater() {
        Kandidat kandidat = enKandidat();

        repository.lagreKandidat(kandidat);
        repository.slettKandidat(kandidat.getAktørId(), enVeileder(), now());

        assertThat(repository.hentNyesteKandidat(kandidat.getAktørId())).isEmpty();
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
        Kandidat kandidat1 = enKandidat("1000000000001");
        Kandidat kandidat2 = enKandidat("1000000000002");
        Kandidat kandidat3 = enKandidat("1000000000003");
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
    public void hentKandidater__skal_returnere_siste_kandidat_etter_lagret_flere_kandidater_med_samme_aktør_id() {
        Kandidat kandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now())
                .build();
        Kandidat nyereKandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now().plusMinutes(1))
                .build();
        Kandidat sisteKandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now().plusMinutes(2))
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
        Kandidat kandidat1 = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now().plusMinutes(1))
                .build();

        Kandidat kandidat2 = kandidatBuilder()
                .aktørId("1000000000002")
                .sistEndret(now())
                .build();

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
    public void hentKandidater__skal_ikke_returnere_slettede_kandidater() {
        Kandidat kandidat1 = enKandidat("1000000000001");
        Kandidat kandidat2 = enKandidat("1000000000002");

        repository.lagreKandidat(kandidat1);
        repository.lagreKandidat(kandidat2);
        repository.slettKandidat(kandidat1.getAktørId(), enVeileder(), now());

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.size()).isEqualTo(1);
    }

    @Test
    public void hentHarTilretteleggingsbehov__skal_returnere_alle_kandidater_inkludert_slettede() {
        Kandidat kandidat1 = enKandidat("1000000000001");
        Kandidat kandidat2 = enKandidat("1000000000002");

        repository.lagreKandidat(kandidat1);
        repository.lagreKandidat(kandidat2);

        repository.slettKandidat(kandidat2.getAktørId(), enVeileder(), now());

        List<HarTilretteleggingsbehov> kandidater = repository.hentHarTilretteleggingsbehov();

        assertThat(kandidater.size()).isEqualTo(2);
        assertThat(kandidater.get(1).isHarTilretteleggingsbehov()).isFalse();
    }

    @Test
    public void hentHarTilretteleggingsbehov__skal_returnere_om_den_siste_registrerte_kandidaten_har_tilretteleggingsbehov() {
        Kandidat kandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now())
                .build();
        Kandidat sisteKandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now().plusMinutes(2))
                .build();

        repository.lagreKandidat(kandidat);
        repository.lagreKandidat(sisteKandidat);

        List<HarTilretteleggingsbehov> kandidater = repository.hentHarTilretteleggingsbehov();

        assertThat(kandidater.size()).isEqualTo(1);
        assertThat(kandidater.get(0).getAktoerId()).isEqualTo(sisteKandidat.getAktørId());
        assertThat(kandidater.get(0).isHarTilretteleggingsbehov()).isTrue();
    }

    @Test
    public void hentHarTilretteleggingsbehov__skal_returnere_den_nyeste_kandidatoppdateringen() {
        Kandidat kandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now())
                .build();

        repository.lagreKandidat(kandidat);
        repository.slettKandidat(kandidat.getAktørId(), enVeileder(), now().plusMinutes(3));

        List<HarTilretteleggingsbehov> kandidatoppdateringer = repository.hentHarTilretteleggingsbehov();

        assertThat(kandidatoppdateringer.size()).isEqualTo(1);
        assertThat(kandidatoppdateringer.get(0).isHarTilretteleggingsbehov()).isFalse();
        assertThat(kandidatoppdateringer.get(0).getAktoerId()).isEqualTo(kandidat.getAktørId());
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_flere_ganger() {
        Kandidat behovTilLagring1 = enKandidat();
        Integer lagretId1 = repository.lagreKandidat(behovTilLagring1);

        Kandidat behovTilLagring2 = enKandidat();
        Integer lagretId2 = repository.lagreKandidat(behovTilLagring2);

        Kandidat uthentetBehov1 = repository.hentKandidat(lagretId1).get();
        Kandidat uthentetBehov2 = repository.hentKandidat(lagretId2).get();

        assertThat(uthentetBehov1.getId()).isEqualTo(lagretId1);
        assertThat(uthentetBehov1).isEqualToIgnoringGivenFields(behovTilLagring1, "id");

        assertThat(uthentetBehov2.getId()).isEqualTo(lagretId2);
        assertThat(uthentetBehov2).isEqualToIgnoringGivenFields(behovTilLagring2, "id");
    }

    @Test
    public void slettKandidat__skal_returnere_empty_hvis_aktør_id_ikke_finnes() {
        String uregistrertAktørId = "1000000000001";

        Optional<Integer> id = repository.slettKandidat(uregistrertAktørId, enVeileder(), now());
        assertThat(id).isEmpty();
    }

    @Test
    public void slettKandidat__skal_returnere_empty_hvis_kandidat_allerede_er_slettet() {
        Kandidat kandidat = enKandidat();
        repository.lagreKandidat(kandidat);
        repository.slettKandidat(kandidat.getAktørId(), enVeileder(), now());

        Optional<Integer> id = repository.slettKandidat(kandidat.getAktørId(), enVeileder(), now());
        assertThat(id).isEmpty();
    }
}
