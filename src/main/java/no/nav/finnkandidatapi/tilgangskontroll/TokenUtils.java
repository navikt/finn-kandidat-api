package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenUtils {

    final static String ISSUER_ISSO = "isso";
    final static String ISSUER_OPENAM = "openam";
    final static String ISSUER_SELVBETJENING = "selvbetjening";

    final static String NAVIDENT_CLAIM = "NAVident";
    final static String NAME_CLAIM = "name";

    private final TokenValidationContextHolder contextHolder;

    @Autowired
    public TokenUtils(TokenValidationContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public String hentOidcToken() {
        String issuer = erInnloggetMedAzureAD() ? ISSUER_ISSO : ISSUER_OPENAM;
        return contextHolder.getTokenValidationContext().getJwtToken(issuer).getTokenAsString();
    }

    private Veileder byggVeilederMedAzureAdClaims(JwtTokenClaims claims) {
        String navIdent = claims.get(NAVIDENT_CLAIM).toString();
        String name = claims.get(NAME_CLAIM).toString();

        return new Veileder(navIdent, name);
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetMedAzureAD()) {
            JwtTokenClaims claims = contextHolder.getTokenValidationContext().getClaims(ISSUER_ISSO);
            return byggVeilederMedAzureAdClaims(claims);
        } else if (erInnloggetMedOpenAM()) {
            String navIdent = contextHolder.getTokenValidationContext().getClaims(ISSUER_OPENAM).getSubject();
            return new Veileder(navIdent, "");
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
        Optional<String> navIdent = Optional.ofNullable(contextHolder.getTokenValidationContext().getClaims(ISSUER_ISSO))
                .map(claims -> claims.get(NAVIDENT_CLAIM).toString())
                .filter(this::erNAVIdent);
        return navIdent.isPresent();
    }

    private boolean erInnloggetMedOpenAM() {
        Optional<String> navIdent = Optional.ofNullable(contextHolder.getTokenValidationContext().getClaims(ISSUER_OPENAM))
                .map(JwtTokenClaims::getSubject)
                .filter(this::erNAVIdent);
        return navIdent.isPresent();
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
