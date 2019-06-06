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
    public final static String ISSUER_ISSO = "isso";
    public final static String ISSUER_ISSO_OPENAM = "isso-openam";

    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public TokenUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    // TODO fjern
    public String hentIssuer() {
        if (erInnloggetNavAnsattMedOpenAMToken()) {
            return "OpenAM";
        } else if (erInnloggetNavAnsattMedAzureADToken()) {
            return "AzureAD";
        } else {
            return "ingen";
        }
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetNavAnsattMedOpenAMToken()) {
            String ident = contextHolder.getOIDCValidationContext().getClaims(ISSUER_ISSO_OPENAM).getSubject();
            return new Veileder(ident);

        } else if (erInnloggetNavAnsattMedAzureADToken()) {
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

    private boolean erInnloggetNavAnsattMedAzureADToken() {
        Optional<String> navIdent = hentClaimSet(ISSUER_ISSO)
                .map(jwtClaimsSet -> (String) jwtClaimsSet.getClaims().get("NAVident"))
                .filter(this::erNAVIdent);
        return navIdent.isPresent();
    }

    private boolean erInnloggetNavAnsattMedOpenAMToken() {
        OIDCClaims claims = contextHolder.getOIDCValidationContext().getClaims(ISSUER_ISSO_OPENAM);
        if (claims == null) {
            return false;
        }
        return erNAVIdent(claims.getSubject());
    }

    private boolean erNAVIdent(String str) {
        return (str != null) && str.matches("^[A-Z][0-9]{6}");
    }

}
