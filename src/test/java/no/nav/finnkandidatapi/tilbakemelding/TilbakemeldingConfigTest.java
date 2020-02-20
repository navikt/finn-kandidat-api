package no.nav.finnkandidatapi.tilbakemelding;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TilbakemeldingConfigTest {
    @Test
    public void skal_oversette_enkeltstreng_med_navidenter_til_liste() {
        String identer = "X12345,Y12345,Z12345";
        TilbakemeldingConfig config = new TilbakemeldingConfig(identer);
        assertThat(config.getNavIdenterSomHarLesetilgangTilTilbakemeldinger()).containsExactly(
                "X12345",
                "Y12345",
                "Z12345"
        );
    }
}