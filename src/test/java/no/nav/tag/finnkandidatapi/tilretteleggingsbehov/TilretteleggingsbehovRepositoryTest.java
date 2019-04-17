package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static no.nav.tag.finnkandidatapi.TestData.etTilretteleggingsbehov;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TilretteleggingsbehovRepositoryTest {

    @Autowired
    private TilretteleggingsbehovRepository repository;

    @After
    public void tearDown() {
        repository.deleteAll();
    }

    @Test
    public void skal_lagre_og_hente_ut() {
        Tilretteleggingsbehov behovTilLagring = etTilretteleggingsbehov();

        Tilretteleggingsbehov lagretBehov = repository.save(behovTilLagring);
        Tilretteleggingsbehov uthentetBehov = repository.findById(lagretBehov.getId()).get();

        assertThat(uthentetBehov.getId()).isGreaterThan(0);
        assertThat(uthentetBehov.getOpprettet()).isEqualToIgnoringNanos(behovTilLagring.getOpprettet());
        assertThat(uthentetBehov.getOpprettetAvIdent()).isEqualTo(behovTilLagring.getOpprettetAvIdent());
        assertThat(uthentetBehov.getBrukerFnr()).isEqualTo(behovTilLagring.getBrukerFnr());
        assertThat(uthentetBehov.getArbeidstid()).isEqualTo(behovTilLagring.getArbeidstid());
        assertThat(uthentetBehov.getFysisk()).containsExactlyInAnyOrderElementsOf(behovTilLagring.getFysisk());
    }
}
