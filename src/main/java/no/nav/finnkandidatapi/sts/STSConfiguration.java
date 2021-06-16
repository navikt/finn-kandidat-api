package no.nav.finnkandidatapi.sts;

import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class STSConfiguration {

    @Value("${SERVICEBRUKER_BRUKERNAVN}")
    private String brukernavn;

    @Value("${SERVICEBRUKER_PASSORD}")
    private String passord;

    @Value("${sts.url}")
    String stsUrl;

    @Bean
    @Profile("!local")
    public SystemUserTokenProvider systemUserTokenProvider() {
        return new NaisSystemUserTokenProvider(stsUrl + "/sts/.well-known/openid-configuration", brukernavn, passord);
    }

    @Bean
    @Profile("local")
    public SystemUserTokenProvider mockSystemUserTokenProvider() {
        return () -> "";
    }

}
