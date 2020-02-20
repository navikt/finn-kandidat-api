package no.nav.finnkandidatapi.tilbakemelding;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@Getter
public class TilbakemeldingConfig {

    private final List<String> navIdenterSomHarLesetilgangTilTilbakemeldinger;

    public TilbakemeldingConfig(
            @Value("${tilgangskontroll.tilbakemeldinger}") String listeMedNavIdenter
    ) {
        this.navIdenterSomHarLesetilgangTilTilbakemeldinger = Arrays.asList(listeMedNavIdenter.split(","));
    }
}
