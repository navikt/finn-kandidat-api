package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TilgangskontrollConfig {
    @Bean
    public Pep veilarbPep() {
        return new VeilarbPep("https://wasapp-q0.adeo.no/asm-pdp/authorize", "srvusername", "srvpassword"); // TODO Are
    }
}
