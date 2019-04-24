package no.nav.tag.finnkandidatapi.kandidat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static no.nav.tag.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KandidatRepositoryTest {

    @Autowired
    private KandidatRepository repository;

    @Test
    public void skal_kunne_lagre_og_hente_kandidat() {
        Kandidat behovTilLagring = enKandidat();

        Integer lagretId = repository.lagreKandidat(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId);

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null_og_tomme_set() {
        Kandidat behovTilLagring = enKandidatMedNullOgTommeSet();

        Integer lagretId = repository.lagreKandidat(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId);

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null() {
        Kandidat behovTilLagring = enKandidatMedBareNull();

        Integer lagretId = repository.lagreKandidat(behovTilLagring);
        Kandidat uthentetBehov = repository.hentKandidat(lagretId);

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(enKandidatMedNullOgTommeSet(), "id");
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_flere_ganger() {
        Kandidat behovTilLagring1 = enKandidat();
        Integer lagretId1 = repository.lagreKandidat(behovTilLagring1);

        Kandidat behovTilLagring2 = enKandidat();
        Integer lagretId2 = repository.lagreKandidat(behovTilLagring2);

        Kandidat uthentetBehov1 = repository.hentKandidat(lagretId1);
        Kandidat uthentetBehov2 = repository.hentKandidat(lagretId2);

        assertThat(uthentetBehov1.getId()).isEqualTo(1);
        assertThat(uthentetBehov1).isEqualToIgnoringGivenFields(behovTilLagring1, "id");

        assertThat(uthentetBehov2.getId()).isEqualTo(2);
        assertThat(uthentetBehov2).isEqualToIgnoringGivenFields(behovTilLagring2, "id");
    }
}
