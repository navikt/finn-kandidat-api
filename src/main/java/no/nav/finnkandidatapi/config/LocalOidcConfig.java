package no.nav.finnkandidatapi.config;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

// TODO: Sjekk dette er riktig
@Configuration
//@Import(TokenGeneratorConfiguration.class)
@EnableMockOAuth2Server
@Profile({"local"})
public class LocalOidcConfig {
}
