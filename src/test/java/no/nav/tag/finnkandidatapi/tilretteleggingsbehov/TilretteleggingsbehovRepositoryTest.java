package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

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

    @Test
    public void skal_kunne_lagre_og_hente_tilretteleggingsbehov() {
        Tilretteleggingsbehov behovTilLagring = etTilretteleggingsbehov();

        Integer lagretId = repository.lagreTilretteleggingsbehov(behovTilLagring);
        Tilretteleggingsbehov uthentetBehov = repository.hentTilretteleggingsbehov(lagretId);

        assertThat(uthentetBehov.getId()).isGreaterThan(0);
        assertThat(uthentetBehov.getOpprettet()).isEqualToIgnoringNanos(behovTilLagring.getOpprettet());
        assertThat(uthentetBehov.getOpprettetAvIdent()).isEqualTo(behovTilLagring.getOpprettetAvIdent());
        assertThat(uthentetBehov.getBrukerFnr()).isEqualTo(behovTilLagring.getBrukerFnr());
        assertThat(uthentetBehov.getArbeidstid()).isEqualTo(behovTilLagring.getArbeidstid());
        assertThat(uthentetBehov.getFysisk()).containsExactlyInAnyOrderElementsOf(behovTilLagring.getFysisk());
        assertThat(uthentetBehov.getArbeidsmiljo()).containsExactlyInAnyOrderElementsOf(behovTilLagring.getArbeidsmiljo());
        assertThat(uthentetBehov.getGrunnleggende()).containsExactlyInAnyOrderElementsOf(behovTilLagring.getGrunnleggende());
    }
}
