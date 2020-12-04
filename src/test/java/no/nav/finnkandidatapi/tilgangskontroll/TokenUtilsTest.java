package no.nav.finnkandidatapi.tilgangskontroll;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static no.nav.finnkandidatapi.TestData.*;
import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_ISSO;
import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_SELVBETJENING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    @Mock
    private TokenValidationContextHolder contextHolder;

    @Test
    public void hentInnloggetVeileder__skal_returnere_riktig_veileder_med_azureAD_token() {
        Veileder veileder = enVeileder();
        værInnloggetMedAzureAD(veileder);
        assertThat(tokenUtils.hentInnloggetVeileder()).isEqualTo(veileder);
    }

    @Test
    public void hentInnloggetVeileder__skal_kaste_exception_hvis_ikke_innlogget() {
        værUinnlogget();
        assertThrows(TilgangskontrollException.class, () ->
                tokenUtils.hentInnloggetVeileder()
        );
    }

    @Test
    public void hentInnloggetBruker__skal_returnere_riktig_bruker() {
        String fnr = etFnr();
        værInnloggetMedSelvBetjening(fnr);
        assertThat(tokenUtils.hentInnloggetBruker()).isEqualTo(fnr);
    }

    @Test
    public void hentInnloggetBruker__skal_kaste_exception_hvis_ikke_innlogget() {
        værUinnlogget();
        assertThrows(TilgangskontrollException.class, () ->
                tokenUtils.hentInnloggetBruker()
        );
    }

    private void værInnloggetMedSelvBetjening(String fnr) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("sub", fnr);
        String encodedToken = new OAuth2TokenProvider().createSignedJWT(builder.build()).serialize();

        JwtToken jwtToken = new JwtToken(encodedToken);
        TokenValidationContext context = new TokenValidationContext(Map.of(ISSUER_SELVBETJENING, jwtToken));
        contextHolder.setTokenValidationContext(context);

        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }

    private void værInnloggetMedAzureAD(Veileder veileder) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("NAVident", veileder.getNavIdent());
        builder.claim("given_name", etFornavn());
        builder.claim("family_name", etEtternavn());

        String encodedToken = new OAuth2TokenProvider().createSignedJWT(builder.build()).serialize();
        JwtToken jwtToken = new JwtToken(encodedToken);
        TokenValidationContext context = new TokenValidationContext(Map.of(ISSUER_ISSO, jwtToken));
        contextHolder.setTokenValidationContext(context);

        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }

    private void værUinnlogget() {
        TokenValidationContext context = new TokenValidationContext(Collections.emptyMap());
        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }
}
