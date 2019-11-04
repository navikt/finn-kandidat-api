package no.nav.tag.finnkandidatapi.unleash;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import no.nav.tag.finnkandidatapi.unleash.enhet.ByEnhetStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService.FINN_KANDIDAT_PILOTTILGANG_KONTOR;

@Configuration
public class UnleashConfiguration {

    private static final String APP_NAME = "finn-kandidat-api";

    public static final String HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE =
            "finnkandidat.hartilretteleggingsbehov.producer";

    public static final String HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT =
            "finnkandidat.hent-oppfolgingsbruker-ved-opprett-kandidat";

    @Profile({"dev", "prod"})
    @Bean
    public Unleash unleash(
            ByClusterStrategy byClusterStrategy,
            ByEnhetStrategy byEnhetStrategy,
            @Value("${unleash.url}") String unleashUrl,
            @Value("${spring.profiles.active}") String profile
    ) {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + profile)
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(
                config,
                byClusterStrategy,
                byEnhetStrategy
        );
    }

    @Profile({"local", "mock"})
    @Bean
    public Unleash unleashMock() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable(HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE);
        fakeUnleash.enable(HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT);
        fakeUnleash.enable(FINN_KANDIDAT_PILOTTILGANG_KONTOR);
        return fakeUnleash;
    }
}
