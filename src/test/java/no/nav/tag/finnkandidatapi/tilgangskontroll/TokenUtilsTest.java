package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.AbacAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static no.nav.security.oidc.test.support.JwtTokenGenerator.createSignedJWT;
import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Test
    public void hentInnloggetVeileder__skal_returnere_riktig_veileder() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        assertThat(tokenUtils.hentInnloggetVeileder()).isEqualTo(veileder);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetVeileder__skal_kaste_exception_hvis_ikke_inlogget() {
        værUinnlogget();
        tokenUtils.hentInnloggetVeileder();
    }

    private void værInnloggetSom(Veileder veileder) {
        Map<String, Object> claims = Map.of("NAVident", veileder.getNavIdent());
        OIDCValidationContext context = new OIDCValidationContext();
        TokenContext tokenContext = new TokenContext(TokenUtils.ISSUER_ISSO, "");
        OIDCClaims oidcClaims = new OIDCClaims(createSignedJWT("blablabla", 0, claims, TokenUtils.ISSUER_ISSO, "aud-isso"));
        context.addValidatedToken(TokenUtils.ISSUER_ISSO, tokenContext, oidcClaims);

        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }


    @Test
    public void test() {
        AbacAction.valueOf("READ");
    }

    private void værUinnlogget() {
        OIDCValidationContext context = new OIDCValidationContext();
        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }
}
