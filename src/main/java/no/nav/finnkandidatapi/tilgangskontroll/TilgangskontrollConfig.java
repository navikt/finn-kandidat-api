package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.SpringAuditRequestInfoSupplier;
import no.nav.common.abac.VeilarbPep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TilgangskontrollConfig {

    @Value("${abac.url}")
    private String abacUrl;

    @Value("${SERVICEUSER_USERNAME}")
    private String srvUsername;

    @Value(("${SERVICEUSER_PASSWORD}"))
    private String srvPassword;


    @Bean
    public Pep veilarbPep() {
        String srvPwLength = srvPassword != null ? String.valueOf(srvPassword.length()) : "null";
        log.info("ABAC autentisering konfig: URL=[" + abacUrl + "], srvUsername=[" + srvUsername + "], srvPassword.length=" + srvPwLength);
        return new VeilarbPep(abacUrl, srvUsername, srvPassword, new SpringAuditRequestInfoSupplier());
    }
}
