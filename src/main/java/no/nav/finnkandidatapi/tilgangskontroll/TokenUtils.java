package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class TokenUtils {

    public final static String ISSUER_AZUREAD = "azuread";
    public final static String ISSUER_TOKENX = "tokenx";

    final static String NAVIDENT_CLAIM = "NAVident";
    final static String FULL_NAME_CLAIM = "name";

    private final TokenValidationContextHolder contextHolder;

    @Autowired
    public TokenUtils(TokenValidationContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    private Veileder hentInnloggetVeilederMedAzureAdClaims(JwtTokenClaims claims) {
        String navIdent = claims.getStringClaim(NAVIDENT_CLAIM);
        String fullName = claims.getStringClaim(FULL_NAME_CLAIM);

        return new Veileder(navIdent, fullName);
    }

    public Veileder hentInnloggetVeileder() {
        if (erInnloggetMedAzureAD()) {
            JwtTokenClaims claims = contextHolder.getTokenValidationContext().getClaims(ISSUER_AZUREAD);
            return hentInnloggetVeilederMedAzureAdClaims(claims);
        } else {
            throw new TilgangskontrollException("Veileder er ikke innlogget.");
        }
    }

    public String hentInnloggetBrukersFødselsnummer() {
        return contextHolder.getTokenValidationContext().getJwtTokenAsOptional(ISSUER_TOKENX)
                .map(jwtToken -> jwtToken.getJwtTokenClaims().getStringClaim("pid"))
                .orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget"));
    }

    private boolean erInnloggetMedAzureAD() {
        Optional<String> navIdent = Optional.ofNullable(contextHolder.getTokenValidationContext().getClaims(ISSUER_AZUREAD))
                .map(claims -> claims.get(NAVIDENT_CLAIM).toString())
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
