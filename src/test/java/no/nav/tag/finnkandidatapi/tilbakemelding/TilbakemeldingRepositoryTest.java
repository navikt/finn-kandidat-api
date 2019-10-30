package no.nav.tag.finnkandidatapi.tilbakemelding;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class TilbakemeldingRepositoryTest {

    @Autowired
    private TilbakemeldingRepository repository;

    @Before
    public void setUp() {
        repository.slettAlleTilbakemeldinger();
    }

    @Test
    public void skal_kunne_lagre_tilbakemelding_i_repo() {
        Tilbakemelding tilbakemelding = new Tilbakemelding(Behov.ARBEIDSMILJØ, "Min tilbakemelding");
        repository.lagreTilbakemelding(tilbakemelding);
    }

    @Test
    public void skal_kunne_hente_ut_alle_tilbakemeldinger() {

        Arrays.asList(
                new Tilbakemelding(Behov.ARBEIDSMILJØ, "Min tilbakemelding"),
                new Tilbakemelding(Behov.ARBEIDSTID, "Min tilbakemelding2"),
                new Tilbakemelding(Behov.ARBEIDSMILJØ, "Min tilbakemelding3")
        ).forEach(repository::lagreTilbakemelding);

        assertThat(repository.hentAlleTilbakemeldinger()).containsExactly(
                new Tilbakemelding(Behov.ARBEIDSMILJØ, "Min tilbakemelding"),
                new Tilbakemelding(Behov.ARBEIDSTID, "Min tilbakemelding2"),
                new Tilbakemelding(Behov.ARBEIDSMILJØ, "Min tilbakemelding3")
        );
    }

}
