package no.nav.finnkandidatapi.config;

import no.nav.security.token.support.spring.test.MockOAuth2ServerAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("local")
@Configuration
@Import(MockOAuth2ServerAutoConfiguration.class)
public class LocalOidcConfig {
}
