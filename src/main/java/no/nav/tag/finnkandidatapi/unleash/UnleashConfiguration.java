package no.nav.tag.finnkandidatapi.unleash;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class UnleashConfiguration {

    private static final String APP_NAME = "finn-kandidat-api";

    public static final String HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE =
            "finnkandidat.hartilretteleggingsbehov.producer";

    @Profile({"dev", "prod"})
    @Bean
    public Unleash unleash(
            ByEnvironmentStrategy byEnvironmentStrategy,
            @Value("${unleash.url}") String unleashUrl,
            @Value("${spring.profiles.active}") String profile
    ) {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + profile)
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(config, byEnvironmentStrategy);
    }

    @Profile({"local", "mock"})
    @Bean
    public Unleash unleashMock() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable(HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE);
        return fakeUnleash;
    }
}
