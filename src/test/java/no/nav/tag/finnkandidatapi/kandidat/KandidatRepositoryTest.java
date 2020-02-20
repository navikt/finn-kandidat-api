package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
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

import static no.nav.tag.finnkandidatapi.TestData.*;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ARBEIDSSTILLING;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ERGONOMI;
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

        Integer lagretId = repository.lagreKandidatSomVeileder(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null_og_tomme_set() {
        Kandidat behovTilLagring = enKandidatMedNullOgTommeSet();

        Integer lagretId = repository.lagreKandidatSomVeileder(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null() {
        Kandidat behovTilLagring = enKandidatMedBareNull();

        Integer lagretId = repository.lagreKandidatSomVeileder(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(enKandidatMedNullOgTommeSet(), "id");
    }

    @Test
    public void hentNyesteKandidat__skal_returnere_nyeste_versjon_av_tilretteleggingsbehov_for_en_kandidat() {
        Kandidat kandidat1 = enKandidat("kandidat1");
        kandidat1.setFysiskeBehov(Set.of(ARBEIDSSTILLING));
        repository.lagreKandidatSomVeileder(kandidat1);

        Kandidat kandidat2 = enKandidat("kandidat2");
        kandidat1.setArbeidstidBehov(Set.of(ArbeidstidBehov.FLEKSIBEL));
        repository.lagreKandidatSomVeileder(kandidat2);

        Kandidat kandidat1Endring = enKandidat(kandidat1.getAktørId());
        kandidat1Endring.setFysiskeBehov(Set.of(ERGONOMI));
        repository.lagreKandidatSomVeileder(kandidat1Endring);

        Kandidat kandidat2Endring = enKandidat(kandidat2.getAktørId());
        kandidat2Endring.setArbeidstidBehov(Set.of(ArbeidstidBehov.BORTE_FASTE_DAGER_ELLER_TIDER));
        repository.lagreKandidatSomVeileder(kandidat2Endring);

        Kandidat nyesteForKandidat1 = repository.hentNyesteKandidat(kandidat1.getAktørId()).get();

        assertThat(nyesteForKandidat1).isEqualToIgnoringGivenFields(kandidat1Endring, "id", "sistEndret");
    }

    @Test
    public void hentNyesteKandidat__skal_ikke_returnere_slettede_kandidater() {
        Kandidat kandidat = enKandidat();

        repository.lagreKandidatSomVeileder(kandidat);
        repository.slettKandidatSomVeileder(kandidat.getAktørId(), enVeileder(), now());

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
        repository.lagreKandidatSomVeileder(kandidat1);
        repository.lagreKandidatSomVeileder(kandidat2);
        repository.lagreKandidatSomVeileder(kandidat3);

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.size()).isEqualTo(3);
        assertThat(kandidater.get(0)).isEqualToIgnoringGivenFields(kandidat1, "id");
        assertThat(kandidater.get(1)).isEqualToIgnoringGivenFields(kandidat2, "id");
        assertThat(kandidater.get(2)).isEqualToIgnoringGivenFields(kandidat3, "id");
    }

    @Test
    public void hentKandidater__skal_returnere_nyeste_versjon_av_hver_kandidat_etter_flere_endringer() {
        Kandidat kandidat1 = kandidatBuilder()
                .aktørId("kand111")
                .sistEndret(now())
                .build();
        repository.lagreKandidatSomVeileder(kandidat1);

        Kandidat kandidat2 = kandidatBuilder()
                .aktørId("kand222")
                .sistEndret(now())
                .build();
        repository.lagreKandidatSomVeileder(kandidat2);

        Kandidat kandidat1Endring1 = kandidatBuilder()
                .aktørId(kandidat1.getAktørId())
                .sistEndret(now().plusMinutes(1))
                .build();
        repository.lagreKandidatSomVeileder(kandidat1Endring1);

        Kandidat kandidat2Endring1 = kandidatBuilder()
                .aktørId(kandidat2.getAktørId())
                .sistEndret(now().plusMinutes(1))
                .build();
        repository.lagreKandidatSomVeileder(kandidat2Endring1);

        Kandidat kandidat1Endring2 = kandidatBuilder()
                .aktørId(kandidat1.getAktørId())
                .sistEndret(now().plusMinutes(2))
                .build();
        repository.lagreKandidatSomVeileder(kandidat1Endring2);

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.size()).isEqualTo(2);
        assertThat(kandidater.get(0)).isEqualToIgnoringGivenFields(kandidat2Endring1, "id");
        assertThat(kandidater.get(1)).isEqualToIgnoringGivenFields(kandidat1Endring2, "id");
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

        repository.lagreKandidatSomVeileder(kandidat1);
        repository.lagreKandidatSomVeileder(kandidat2);

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

        repository.lagreKandidatSomVeileder(kandidat1);
        repository.lagreKandidatSomVeileder(kandidat2);
        repository.slettKandidatSomVeileder(kandidat1.getAktørId(), enVeileder(), now());

        List<Kandidat> kandidater = repository.hentKandidater();

        assertThat(kandidater.size()).isEqualTo(1);
        assertThat(kandidater.get(0)).isEqualToIgnoringGivenFields(kandidat2, "id");
    }

    @Test
    public void hentHarTilretteleggingsbehov__skal_returnere_alle_kandidater_inkludert_slettede() {
        Kandidat kandidat1 = enKandidat("1000000000001");
        Kandidat kandidat2 = enKandidat("1000000000002");

        repository.lagreKandidatSomVeileder(kandidat1);
        repository.lagreKandidatSomVeileder(kandidat2);

        repository.slettKandidatSomVeileder(kandidat2.getAktørId(), enVeileder(), now());

        List<HarTilretteleggingsbehov> kandidater = repository.hentHarTilretteleggingsbehov();

        assertThat(kandidater.size()).isEqualTo(2);
        assertThat(kandidater.get(0).isHarTilretteleggingsbehov()).isTrue();
        assertThat(kandidater.get(0).getBehov()).containsExactlyInAnyOrder(
                ArbeidstidBehov.behovskategori,
                FysiskBehov.behovskategori,
                ArbeidsmiljøBehov.behovskategori,
                GrunnleggendeBehov.behovskategori
        );
        assertThat(kandidater.get(1).isHarTilretteleggingsbehov()).isFalse();
        assertThat(kandidater.get(1).getBehov()).isEmpty();
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

        repository.lagreKandidatSomVeileder(kandidat);
        repository.lagreKandidatSomVeileder(sisteKandidat);

        List<HarTilretteleggingsbehov> kandidater = repository.hentHarTilretteleggingsbehov();

        assertThat(kandidater.size()).isEqualTo(1);
        assertThat(kandidater.get(0).getAktoerId()).isEqualTo(sisteKandidat.getAktørId());
        assertThat(kandidater.get(0).isHarTilretteleggingsbehov()).isTrue();
        assertThat(kandidater.get(0).getBehov()).containsExactlyInAnyOrder(
                ArbeidstidBehov.behovskategori,
                FysiskBehov.behovskategori,
                ArbeidsmiljøBehov.behovskategori,
                GrunnleggendeBehov.behovskategori
        );
    }

    @Test
    public void hentHarTilretteleggingsbehov__skal_returnere_den_nyeste_kandidatoppdateringen() {
        Kandidat kandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .sistEndret(now())
                .build();

        repository.lagreKandidatSomVeileder(kandidat);
        repository.slettKandidatSomVeileder(kandidat.getAktørId(), enVeileder(), now().plusMinutes(3));

        List<HarTilretteleggingsbehov> kandidatoppdateringer = repository.hentHarTilretteleggingsbehov();

        assertThat(kandidatoppdateringer.size()).isEqualTo(1);
        assertThat(kandidatoppdateringer.get(0).isHarTilretteleggingsbehov()).isFalse();
        assertThat(kandidatoppdateringer.get(0).getAktoerId()).isEqualTo(kandidat.getAktørId());
    }

    @Test
    public void hentHarTilretteleggingsbehov__skal_returnere_hvorvidt_kandidaten_har_tilretteleggingsbehov() {
        Kandidat kandidat = kandidatBuilder()
                .aktørId("1000000000001")
                .build();
        Kandidat kandidatUtenTilretteleggingsbehov = kandidatBuilder()
                .aktørId("1000000000002")
                .build();

        repository.lagreKandidatSomVeileder(kandidat);
        repository.lagreKandidatSomVeileder(kandidatUtenTilretteleggingsbehov);
        repository.slettKandidatSomVeileder(kandidatUtenTilretteleggingsbehov.getAktørId(), enVeileder(), now());

        Optional<HarTilretteleggingsbehov> harTilretteleggingsbehov =
                repository.hentHarTilretteleggingsbehov(kandidat.getAktørId());
        Optional<HarTilretteleggingsbehov> harIkkeTilretteleggingsbehov =
                repository.hentHarTilretteleggingsbehov(kandidatUtenTilretteleggingsbehov.getAktørId());

        assertThat(harTilretteleggingsbehov.get().isHarTilretteleggingsbehov()).isTrue();
        assertThat(harTilretteleggingsbehov.get().getBehov()).containsExactlyInAnyOrder(
                ArbeidstidBehov.behovskategori,
                FysiskBehov.behovskategori,
                ArbeidsmiljøBehov.behovskategori,
                GrunnleggendeBehov.behovskategori
        );
        assertThat(harIkkeTilretteleggingsbehov.get().isHarTilretteleggingsbehov()).isFalse();
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_flere_ganger() {
        Kandidat behovTilLagring1 = enKandidat();
        Integer lagretId1 = repository.lagreKandidatSomVeileder(behovTilLagring1);

        Kandidat behovTilLagring2 = enKandidat();
        Integer lagretId2 = repository.lagreKandidatSomVeileder(behovTilLagring2);

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

        Optional<Integer> id = repository.slettKandidatSomVeileder(uregistrertAktørId, enVeileder(), now());
        assertThat(id).isEmpty();
    }

    @Test
    public void slettKandidat__skal_returnere_empty_hvis_kandidat_allerede_er_slettet() {
        Kandidat kandidat = enKandidat();
        repository.lagreKandidatSomVeileder(kandidat);
        repository.slettKandidatSomVeileder(kandidat.getAktørId(), enVeileder(), now());

        Optional<Integer> id = repository.slettKandidatSomVeileder(kandidat.getAktørId(), enVeileder(), now());
        assertThat(id).isEmpty();
    }

    @Test
    public void oppdaterNavKontor__skal_oppdatere_nav_kontor_på_alle_rader() {
        Kandidat kandidat = enKandidat();
        repository.lagreKandidatSomVeileder(kandidat);
        repository.lagreKandidatSomVeileder(kandidat);

        int antallOppdaterteRader = repository.oppdaterNavKontor(kandidat.getFnr(), "1337");
        Kandidat kandidatMedNyttKontor = repository.hentNyesteKandidat(kandidat.getAktørId()).get();

        assertThat(antallOppdaterteRader).isEqualTo(2);
        assertThat(kandidatMedNyttKontor.getNavKontor()).isEqualTo("1337");
        assertThat(kandidatMedNyttKontor).isEqualToIgnoringGivenFields(kandidat, "id", "navKontor");
    }

    @Test
    public void oppdaterNavKontor__skal_ikke_oppdatere_navKontor_hvis_fnr_er_null() {
        Kandidat kandidat = enKandidat();
        kandidat.setFnr(null);
        repository.lagreKandidatSomVeileder(kandidat);

        int antallOppdaterteRader = repository.oppdaterNavKontor(null, "1337");
        assertThat(antallOppdaterteRader).isEqualTo(0);
    }


}
