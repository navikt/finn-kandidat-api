package no.nav.finnkandidatapi.config;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

// TODO: Sjekk dette er riktig
//@Import(TokenGeneratorConfiguration.class)
@Configuration
@Profile("local")
//@EnableMockOAuth2Server
public class LocalOidcConfig {
}
