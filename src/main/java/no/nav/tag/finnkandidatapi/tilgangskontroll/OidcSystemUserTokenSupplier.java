package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class OidcSystemUserTokenSupplier implements Supplier<String> {
    private final STSClient stsClient;

    @Autowired
    public OidcSystemUserTokenSupplier(STSClient stsClient) {
        this.stsClient = stsClient;
    }

    @Override
    public String get() {
        return stsClient.getToken().getAccess_token();
    }
}
