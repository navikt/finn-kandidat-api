package no.nav.finnkandidatapi.sts;

import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
public class STSConfiguration {

    @Value("${SERVICEBRUKER_BRUKERNAVN}")
    private String brukernavn;

    @Value("${SERVICEBRUKER_PASSORD}")
    private String passord;

    @Value("${sts.url}")
    String stsUrl;


    @Bean
    public RestTemplate stsBasicAuthRestTemplate() {
        return new RestTemplateBuilder()
                .basicAuthentication(brukernavn, passord)
                .build();
    }

    @Bean
    @Profile("!local")
    public SystemUserTokenProvider systemUserTokenProvider() {
        return new NaisSystemUserTokenProvider(stsUrl, brukernavn, passord);
    }

    @Bean
    @Profile("local")
    public SystemUserTokenProvider mockSystemUserTokenProvider() {
        return () -> "";
    }

}
