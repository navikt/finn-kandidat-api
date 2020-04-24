package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.enMidlertidigUtilgjengelig;
import static no.nav.finnkandidatapi.TestData.enMidlertidigUtilgjengeligMedBareNull;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class MidlertidigUtilgjengeligRepositoryTest {

    @Autowired
    private MidlertidigUtilgjengeligRepository repository;

    @Test
    public void skal_kunne_lagre_og_hente_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig behovTilLagring = enMidlertidigUtilgjengelig("11100000000");

        String[] skalVæreNull = {"sistEndretAvIdent", "sistEndretAvNavn"};

        Integer lagretId = repository.lagreMidlertidigUtilgjengelig(behovTilLagring);
        MidlertidigUtilgjengelig uthentetBehov = repository.hentMidlertidigUtilgjengelig(behovTilLagring.getAktørId()).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, skalVæreNull)
                .extracting(skalVæreNull).containsOnlyNulls();
    }

    @Test
    public void skal_kunne_lagre_og_hente_ut_med_bare_obligatoriske_felt() {
        MidlertidigUtilgjengelig behovTilLagring = enMidlertidigUtilgjengeligMedBareNull();
        behovTilLagring.setFraDato(LocalDateTime.of(2020, 1, 1, 0, 0));

        repository.lagreMidlertidigUtilgjengelig(behovTilLagring);
        MidlertidigUtilgjengelig uthentetBehov = repository.hentMidlertidigUtilgjengelig(behovTilLagring.getAktørId()).get();

        assertThat(uthentetBehov).isEqualToIgnoringGivenFields(behovTilLagring, "id");
    }

    @Test
    public void hentMidlertidigUtilgjengelig__skal_håndtere_henting_av_ikke_eksisterende() {
        boolean midlertidigUtilgjengeligEksisterer = repository.hentMidlertidigUtilgjengelig("finnes ikke").isPresent();
        assertThat(midlertidigUtilgjengeligEksisterer).isFalse();
    }

    @Test
    public void hentMidlertidigUtilgjengelig_skal_håndtere_henting_av_ikke_eksisterende() {
        boolean midlertidigUtilgjengeligEksisterer = repository.hentMidlertidigUtilgjengelig("100").isPresent();
        assertThat(midlertidigUtilgjengeligEksisterer).isFalse();
    }

    @Test
    public void hentMidlertidigUtilgjengelig_skal_returnere_lagrede() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig1 = enMidlertidigUtilgjengelig("1000000000001");
        MidlertidigUtilgjengelig midlertidigUtilgjengelig2 = enMidlertidigUtilgjengelig("1000000000002");
        MidlertidigUtilgjengelig midlertidigUtilgjengelig3 = enMidlertidigUtilgjengelig("1000000000003");
        repository.lagreMidlertidigUtilgjengelig(midlertidigUtilgjengelig1);
        repository.lagreMidlertidigUtilgjengelig(midlertidigUtilgjengelig2);
        repository.lagreMidlertidigUtilgjengelig(midlertidigUtilgjengelig3);

        var lagret1 = repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig1.getAktørId());
        var lagret2 = repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig2.getAktørId());
        var lagret3 = repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig3.getAktørId());

        String[] skalVæreNull = {"sistEndretAvIdent", "sistEndretAvNavn"};

        assertThat(lagret1).isNotEmpty().get()
                .isEqualToIgnoringGivenFields(midlertidigUtilgjengelig1, skalVæreNull)
                .extracting(skalVæreNull).containsOnlyNulls();
        assertThat(lagret2).isNotEmpty().get()
                .isEqualToIgnoringGivenFields(midlertidigUtilgjengelig2, skalVæreNull)
                .extracting(skalVæreNull).containsOnlyNulls();
        assertThat(lagret3).isNotEmpty().get()
                .isEqualToIgnoringGivenFields(midlertidigUtilgjengelig3, skalVæreNull)
                .extracting(skalVæreNull).containsOnlyNulls();
    }

    @Test
    public void hentMidlertidigUtilgjengelig_skal_ikke_returnere_slettet_aktør() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = enMidlertidigUtilgjengelig("1000000000033");

        repository.lagreMidlertidigUtilgjengelig(midlertidigUtilgjengelig);
        repository.slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());

        Optional<MidlertidigUtilgjengelig> respons = repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());
        assertThat(respons).isEmpty();
    }

    @Test
    public void slettKandidat__skal_returnere_empty_hvis_aktør_id_ikke_finnes() {
        String uregistrertAktørId = "finnes ikke";

        Integer id = repository.slettMidlertidigUtilgjengelig(uregistrertAktørId);
        assertThat(id).isEqualTo(0);
    }
}
