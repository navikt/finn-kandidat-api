package no.nav.finnkandidatapi.pdl;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.finnkandidatapi.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdlClientConfig {

    @Bean
    public AktorOppslagClient aktorOppslagClient(@Value("${pdl.url}") String url, STSClient stsClient) {
        return new PdlAktorOppslagClient(
                url,
                () -> stsClient.hentSTSToken().getAccessToken(),
                () -> stsClient.hentSTSToken().getAccessToken()
         );
    }
}
