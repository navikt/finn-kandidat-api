package no.nav.tag.finnkandidatapi.tilgangskontroll;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenUtils {

    final static String ISSUER_ISSO = "isso";

    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public TokenUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public String hentOidcToken() {
        // TODO: sjekke om den fins (hasValidTokenFor) med optional
        return contextHolder.getOIDCValidationContext().getToken(ISSUER_ISSO).getIdToken();
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetNavAnsattMedAzureADToken()) {
            String navIdent = hentClaim(ISSUER_ISSO, "NAVident")
                    .orElseThrow(() -> new TilgangskontrollException("Innlogget bruker er ikke veileder."));
            return new Veileder(navIdent);
        } else {
            throw new TilgangskontrollException("Bruker er ikke innlogget.");
        }
    }

    private boolean erInnloggetNavAnsattMedAzureADToken() {
        Optional<String> navIdent = hentClaimSet(ISSUER_ISSO)
                .map(jwtClaimsSet -> (String) jwtClaimsSet.getClaims().get("NAVident"))
                .filter(this::erNAVIdent);
        return navIdent.isPresent();
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JWTClaimsSet> claimSet = hentClaimSet(issuer);
        return claimSet.map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(OIDCClaims::getClaimSet);
    }

    private boolean erNAVIdent(String str) {
        return (str != null) && str.matches("^[A-Z][0-9]{6}");
    }

}
