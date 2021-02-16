package no.nav.finnkandidatapi.vedtak;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static no.nav.finnkandidatapi.TestData.etTomtVedtak;
import static no.nav.finnkandidatapi.TestData.etVedtak;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class VedtakRepositoryTest {

    @Autowired
    private VedtakRepository repository;

    @Before
    public void setUp() {
        repository.fysiskSlettAlleVedtak();
    }

    @Test
    public void skal_kunne_lagre_og_hente_vedtak() {
        Vedtak vedtak = etVedtak();
        Long lagretId = repository.lagreVedtak(vedtak);
        Vedtak uthentetVedtak = repository.hentVedtak(lagretId).get();

        assertThat(uthentetVedtak).isEqualToIgnoringGivenFields(vedtak, "id" );
    }

    @Test
    public void vedtak_med_predefinert_id_skal_gis_ny_id_ved_lagring() {

        Vedtak vedtak = etVedtak();
        Long id = 22L;
        vedtak.setId(id);
        Long lagretId = repository.lagreVedtak(vedtak);
        Vedtak uthentetVedtak = repository.hentVedtak(lagretId).get();

        assertThat(uthentetVedtak.getId()).isNotEqualTo(vedtak.getId());
        assertThat(lagretId).isNotEqualTo(id);
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null() {
        Vedtak vedtak = etTomtVedtak();
        Long lagretId = repository.lagreVedtak(vedtak);
        Vedtak uthentetVedtak = repository.hentVedtak(lagretId).get();

        assertThat(uthentetVedtak).isEqualToIgnoringGivenFields(vedtak, "id" );
    }

    @Test
    public void hentNyesteVersjonAvNyesteVedtakForAktør_skal_håndtere_henting_av_ikke_eksisterende_aktorId() {
        boolean eksisterer = repository.hentNyesteVersjonAvNyesteVedtakForAktør("finnes ikke" ).isPresent();

        assertThat(eksisterer).isFalse();
    }

    @Test
    public void hentNyesteVersjonAvNyesteVedtakForAktør_skal_hente_siste_versjon_av_samme_vedtak() {
        Vedtak vedtak = etVedtak();
        Long lagretId1 = repository.lagreVedtak(vedtak);
        Long lagretId2 = repository.lagreVedtak(vedtak);

        Vedtak uthentetVedtak = repository.hentNyesteVersjonAvNyesteVedtakForAktør(vedtak.getAktørId()).get();

        assertThat(lagretId1).isNotEqualTo(lagretId2);
        assertThat(uthentetVedtak.getId()).isEqualTo(lagretId2);
    }

    @Test
    public void hentNyesteVersjonAvNyesteVedtakForAktør_skal_hente_siste_versjon_av_siste_vedtak() {
        Vedtak vedtak = etVedtak();
        Long lagretId1 = repository.lagreVedtak(vedtak);
        Long lagretId2 = repository.lagreVedtak(vedtak);

        vedtak.setVedtakId(4545L);
        vedtak.setFraDato(vedtak.getFraDato().minusDays(4));
        Long lagretId3 = repository.lagreVedtak(vedtak);
        Long lagretId4 = repository.lagreVedtak(vedtak);

        Vedtak uthentetVedtak = repository.hentNyesteVersjonAvNyesteVedtakForAktør(vedtak.getAktørId()).get();

        assertThat(lagretId1).isNotEqualTo(lagretId2);
        assertThat(lagretId2).isNotEqualTo(lagretId3);
        assertThat(lagretId3).isNotEqualTo(lagretId4);
        assertThat(uthentetVedtak.getId()).isEqualTo(lagretId2);
    }

    @Test
    public void hentNyesteVersjonAvNyesteVedtakForAktør_skal_ikke_hente_slettede_vedtak() {
        Vedtak vedtak1 = etVedtak();
        Long lagretId1 = repository.lagreVedtak(vedtak1);
        Long lagretId2 = repository.lagreVedtak(vedtak1);

        Vedtak vedtak2 = etVedtak();
        vedtak2.setVedtakId(4545L);
        vedtak2.setFraDato(vedtak1.getFraDato().minusDays(4));
        Long lagretId3 = repository.lagreVedtak(vedtak2);
        Long lagretId4 = repository.lagreVedtak(vedtak2);

        repository.logiskSlettVedtak(vedtak1);

        Vedtak uthentetVedtak = repository.hentNyesteVersjonAvNyesteVedtakForAktør(vedtak1.getAktørId()).get();

        assertThat(lagretId1).isNotEqualTo(lagretId2);
        assertThat(lagretId2).isNotEqualTo(lagretId3);
        assertThat(lagretId3).isNotEqualTo(lagretId4);
        assertThat(uthentetVedtak.getId()).isEqualTo(lagretId4);
    }

    @Test
    public void hent_vedtak_fra_id_skal_hente_slettet() {
        Vedtak vedtak = etVedtak();
        Long lagretId = repository.lagreVedtak(vedtak);
        repository.logiskSlettVedtak(vedtak);
        Vedtak uthentetVedtak = repository.hentVedtak(lagretId).get();

        assertThat(vedtak.isSlettet()).isFalse();
        assertThat(uthentetVedtak).isEqualToIgnoringGivenFields(vedtak, "id", "slettet" );
        assertThat(uthentetVedtak.isSlettet()).isTrue();
    }
}
