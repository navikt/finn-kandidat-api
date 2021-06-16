package no.nav.finnkandidatapi.pdl;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.sts.SystemUserTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdlClientConfig {

    @Bean
    public AktorOppslagClient aktorOppslagClient(@Value("${pdl.url}") String url, SystemUserTokenProvider systemUserTokenProvider) {
        return new PdlAktorOppslagClient(
                url,
                systemUserTokenProvider::getSystemUserToken,
                systemUserTokenProvider::getSystemUserToken
         );
    }
}
