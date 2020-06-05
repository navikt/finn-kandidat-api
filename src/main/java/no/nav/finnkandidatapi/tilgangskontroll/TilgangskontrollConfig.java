package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TilgangskontrollConfig {

    @Value("${abac.url}")
    private String abacUrl;

    @Value("${SERVICEUSER_USERNAME}")
    private String srvUsername;

    @Value(("${SERVICEUSER_PASSWORD}"))
    private String srvPassword;


    @Bean
    public Pep veilarbPep() {
        return new VeilarbPep(abacUrl, srvUsername, srvPassword);
    }
}
