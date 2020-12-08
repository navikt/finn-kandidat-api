package no.nav.security.oidc.test.support.spring;

import no.nav.security.oidc.test.support.FileResourceRetriever;
import no.nav.security.oidc.test.support.JwkGenerator;
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import(TokenGeneratorController.class)
public class TokenGeneratorConfiguration implements WebMvcConfigurer {
    /**
     * To be able to ovverride the oidc validation properties in
     * EnableOIDCTokenValidationConfiguration in oidc-spring-support
     */
    @Bean
    @Primary
    ProxyAwareResourceRetriever overrideOidcResourceRetriever() {
        return new FileResourceRetriever("/local-login/metadata-selvbetjening.json", "/local-login/metadata-isso.json", "/local-login/jwkset.json");
    }

    @Bean
    JwkGenerator jwkGenerator() {
        return new JwkGenerator();
    }
}
