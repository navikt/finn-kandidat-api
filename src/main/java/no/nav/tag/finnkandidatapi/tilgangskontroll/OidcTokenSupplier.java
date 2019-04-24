package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class OidcTokenSupplier implements Supplier<String> {

    private final static String ISSUER_ISSO = "isso";

    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public OidcTokenSupplier(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @Override
    public String get() {
        return contextHolder.getOIDCValidationContext().getToken(ISSUER_ISSO).getIdToken();
    }
}
