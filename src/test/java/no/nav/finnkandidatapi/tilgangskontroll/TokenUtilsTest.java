package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static no.nav.finnkandidatapi.TestData.enVeileder;
import static no.nav.finnkandidatapi.TestData.etFnr;
import static no.nav.security.oidc.test.support.JwtTokenGenerator.createSignedJWT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Test
    public void hentInnloggetVeileder__skal_returnere_riktig_veileder_med_azureAD_token() {
        Veileder veileder = enVeileder();
        værInnloggetMedAzureAD(veileder);
        assertThat(tokenUtils.hentInnloggetVeileder()).isEqualTo(veileder);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetVeileder__skal_kaste_exception_hvis_ikke_innlogget() {
        værUinnlogget();
        tokenUtils.hentInnloggetVeileder();
    }

    @Test
    public void hentInnloggetBruker__skal_returnere_riktig_bruker() {
        String fnr = etFnr();
        værInnloggetMedSelvBetjening(fnr);
        assertThat(tokenUtils.hentInnloggetBruker()).isEqualTo(fnr);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetBruker__skal_kaste_exception_hvis_ikke_innlogget() {
        værUinnlogget();
        tokenUtils.hentInnloggetBruker();
    }

    private void værInnloggetMedSelvBetjening(String fnr) {
        Map<String, Object> claims = Map.of("sub", fnr);
        OIDCValidationContext context = new OIDCValidationContext();
        TokenContext tokenContext = new TokenContext(TokenUtils.ISSUER_SELVBETJENING, "");
        OIDCClaims oidcClaims = new OIDCClaims(createSignedJWT("blablabla", 0, claims, TokenUtils.ISSUER_SELVBETJENING, "aud-selvbetjening"));
        context.addValidatedToken(TokenUtils.ISSUER_SELVBETJENING, tokenContext, oidcClaims);

        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }

    private void værInnloggetMedAzureAD(Veileder veileder) {
        Map<String, Object> claims = Map.of("NAVident", veileder.getNavIdent());
        OIDCValidationContext context = new OIDCValidationContext();
        TokenContext tokenContext = new TokenContext(TokenUtils.ISSUER_ISSO, "");
        OIDCClaims oidcClaims = new OIDCClaims(createSignedJWT("blablabla", 0, claims, TokenUtils.ISSUER_ISSO, "aud-isso"));
        context.addValidatedToken(TokenUtils.ISSUER_ISSO, tokenContext, oidcClaims);

        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }

    private void værUinnlogget() {
        OIDCValidationContext context = new OIDCValidationContext();
        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }
}
