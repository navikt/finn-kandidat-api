package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    final static String ISSUER_ISSO = "isso";
    final static String ISSUER_OPENAM = "openam";
    final static String ISSUER_SELVBETJENING = "selvbetjening";

    private final TokenValidationContextHolder contextHolder;

    @Autowired
    public TokenUtils(TokenValidationContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public String hentOidcToken() {
        String issuer = erInnloggetMedAzureAD() ? ISSUER_ISSO : ISSUER_OPENAM;
        return contextHolder.getTokenValidationContext().getJwtToken(issuer).getTokenAsString();
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetMedAzureAD()) {
            String navIdent = contextHolder.getTokenValidationContext().getClaims(ISSUER_ISSO).get("NAVident").toString();
            return new Veileder(navIdent);
        } else if (erInnloggetMedOpenAM()) {
            String navIdent = contextHolder.getTokenValidationContext().getClaims(ISSUER_OPENAM).getSubject();
            return new Veileder(navIdent);
        } else {
            throw new TilgangskontrollException("Veileder er ikke innlogget.");
        }
    }

    public String hentInnloggetBruker() {
        return contextHolder.getTokenValidationContext().getJwtTokenAsOptional(ISSUER_SELVBETJENING)
                .map(JwtToken::getSubject)
                .orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget"));
    }

    public String hentOidcTokenSelvbetjening() {
        return contextHolder.getTokenValidationContext().getJwtToken(ISSUER_SELVBETJENING).getTokenAsString();
    }

    private boolean erInnloggetMedAzureAD() {
        String navIdent = contextHolder.getTokenValidationContext().getClaims(ISSUER_ISSO).get("NAVident").toString();
        return erNAVIdent(navIdent);
    }

    private boolean erInnloggetMedOpenAM() {
        JwtTokenClaims claims = contextHolder.getTokenValidationContext().getClaims(ISSUER_OPENAM);
        return erNAVIdent(claims.getSubject());
    }

    public boolean harInnloggingsContext() {
        try {
            contextHolder.getTokenValidationContext();
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
