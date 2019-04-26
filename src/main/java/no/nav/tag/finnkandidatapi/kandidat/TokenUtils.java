package no.nav.tag.finnkandidatapi.kandidat;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenUtils {
    public final static String ISSUER_ISSO = "isso";

    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public TokenUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetNavAnsatt()) {
            String navIdent = hentClaim(ISSUER_ISSO, "NAVident")
                    .orElseThrow(() -> new TilgangskontrollException("Innlogget bruker er ikke veileder."));
            return new Veileder(navIdent);
        } else {
            throw new TilgangskontrollException("Bruker er ikke innlogget.");
        }
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JWTClaimsSet> claimSet = hentClaimSet(issuer);
        return claimSet.map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(OIDCClaims::getClaimSet);
    }

    private boolean erInnloggetNavAnsatt() {
        return hentClaimSet(ISSUER_ISSO)
                .map(jwtClaimsSet -> (String) jwtClaimsSet.getClaims().get("NAVident"))
                .isPresent();
    }
}
