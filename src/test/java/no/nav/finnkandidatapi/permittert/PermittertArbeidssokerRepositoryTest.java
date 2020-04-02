package no.nav.finnkandidatapi.permittert;

import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kandidat.*;
import org.assertj.core.api.Assertions;
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

import static no.nav.finnkandidatapi.TestData.*;
import static no.nav.finnkandidatapi.kandidat.Fysisk.ARBEIDSSTILLING;
import static no.nav.finnkandidatapi.kandidat.Fysisk.ERGONOMI;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class PermittertArbeidssokerRepositoryTest {

    @Autowired
    private PermittertArbeidssokerRepository repository;

    @Before
    public void setUp() {
        repository.slettAllePermitterteArbeidssokere();
    }

    @Test
    public void skal_kunne_lagre_og_hente_permittert_arbeidssoker() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        Integer lagretId = repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        PermittertArbeidssoker uthentetPermittertArbeidssoker = repository.hentPermittertArbeidssoker(lagretId).get();

        assertThat(uthentetPermittertArbeidssoker).isEqualToIgnoringGivenFields(permittertArbeidssoker, "id");
    }

    @Test
    public void arbeidssoker_med_predefinert_id_skal_gis_ny_id_ved_lagring() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        Integer id = 100;
        permittertArbeidssoker.setId(id);
        Integer lagretId = repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        PermittertArbeidssoker uthentetPermittertArbeidssoker = repository.hentPermittertArbeidssoker(lagretId).get();

        assertThat(uthentetPermittertArbeidssoker.getId()).isNotEqualTo(permittertArbeidssoker.getId());
        assertThat(lagretId).isNotEqualTo(id);
    }


    @Test
    public void skal_kunne_lagre_og_hente_ut_med_null() {
        PermittertArbeidssoker permittertArbeidssoker = enTomPermittertArbeidssoker();
        Integer lagretId = repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        PermittertArbeidssoker uthentetPermittertArbeidssoker = repository.hentPermittertArbeidssoker(lagretId).get();

        assertThat(uthentetPermittertArbeidssoker).isEqualToIgnoringGivenFields(permittertArbeidssoker, "id");
    }

    @Test
    public void hentNyestePermitterteArbeidssoker_skal_returnere_nyeste_versjon_av_status_fra_veilarb() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        Integer lagretId = repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        PermittertArbeidssoker uthentetPermittertArbeidssoker = repository.hentPermittertArbeidssoker(lagretId).get();
        uthentetPermittertArbeidssoker.setStatusFraVeilarbRegistrering(DinSituasjonSvarFraVeilarbReg.MISTET_JOBBEN.name());
        repository.lagrePermittertArbeidssoker(uthentetPermittertArbeidssoker);
        Optional<PermittertArbeidssoker> nyestePermittertArbeidssoker = repository.hentNyestePermittertArbeidssoker(permittertArbeidssoker.getAktørId());

        assertThat(uthentetPermittertArbeidssoker).isEqualToIgnoringGivenFields(nyestePermittertArbeidssoker.get(), "id", "statusFraVeilarbRegistrering");
    }

    @Test
    public void hentNyestePermitterteArbeidssoker_skal_ikke_returnere_slettede_arbeidssokere() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        repository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());

        assertThat(repository.hentNyestePermittertArbeidssoker(permittertArbeidssoker.getAktørId())).isEmpty();
    }

    @Test
    public void hentNyesteKandidat__skal_håndtere_henting_av_ikke_eksisterende_kandidat() {
        boolean eksisterer = repository.hentNyestePermittertArbeidssoker("finnes ikke").isPresent();

        assertThat(eksisterer).isFalse();
    }

    @Test
    public void slett_skal_returnere_empty_hvis_aktør_id_ikke_finnes() {
        String uregistrertAktørId = "1000000000001";
        Optional<Integer> id = repository.slettPermittertArbeidssoker(uregistrertAktørId);

        assertThat(id).isEmpty();
    }

    @Test
    public void slett_skal_returnere_id() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        Optional<Integer> id = repository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());

        assertThat(id).isNotEmpty();
    }
    @Test
    public void slett_skal_returnere_empty_hvis_allerede_slettet() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        repository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());
        Optional<Integer> id = repository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());

        assertThat(id).isEmpty();
    }
}
