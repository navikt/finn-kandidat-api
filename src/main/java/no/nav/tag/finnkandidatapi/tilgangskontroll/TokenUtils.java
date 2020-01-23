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
    final static String ISSUER_OPENAM = "openam";

    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public TokenUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public String hentOidcToken() {
        String issuer = erInnloggetMedAzureAD() ? ISSUER_ISSO : ISSUER_OPENAM;
        return contextHolder.getOIDCValidationContext().getToken(issuer).getIdToken();
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetMedAzureAD()) {
            String navIdent = hentClaim(ISSUER_ISSO, "NAVident")
                    .orElseThrow(() -> new TilgangskontrollException("Innlogget bruker er ikke veileder."));
            return new Veileder(navIdent);
        } else if (erInnloggetMedOpenAM()) {
            String navIdent = contextHolder.getOIDCValidationContext().getClaims(ISSUER_OPENAM).getSubject();
            return new Veileder(navIdent);
        } else {
            throw new TilgangskontrollException("Bruker er ikke innlogget.");
        }
    }

    private boolean erInnloggetMedAzureAD() {
        Optional<String> navIdent = hentClaimSet(ISSUER_ISSO)
                .map(jwtClaimsSet -> (String) jwtClaimsSet.getClaims().get("NAVident"))
                .filter(this::erNAVIdent);
        return navIdent.isPresent();
    }

    private boolean erInnloggetMedOpenAM() {
        OIDCClaims claims = contextHolder.getOIDCValidationContext().getClaims(ISSUER_OPENAM);
        if (claims == null) {
            return false;
        }

        return erNAVIdent(claims.getSubject());
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JWTClaimsSet> claimSet = hentClaimSet(issuer);
        return claimSet.map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(OIDCClaims::getClaimSet);
    }

    public boolean harInnloggingsContext() {
        try {
            contextHolder.getOIDCValidationContext();
            return true;
        } catch (IllegalStateException exception) {
            // Kaster exception hvis man prøver å hente context utenfor et request initiert av en bruker
            return false;
        }
    }

    private boolean erNAVIdent(String str) {
        return (str != null) && str.matches("^[A-Z][0-9]{6}");
    }

}
