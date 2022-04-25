package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
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
import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    @Mock
    private TokenValidationContextHolder contextHolder;

    private final MockOAuth2Server oAuth2Server = new MockOAuth2Server();

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
        værInnloggetMedIdPorten(fnr);
        assertThat(tokenUtils.hentInnloggetBrukersFødselsnummer()).isEqualTo(fnr);
    }

    @Test
    public void hentInnloggetBruker__skal_kaste_exception_hvis_ikke_innlogget() {
        værUinnlogget();
        assertThrows(TilgangskontrollException.class, () ->
                tokenUtils.hentInnloggetBrukersFødselsnummer()
        );
    }

    private void værInnloggetMedIdPorten(String fnr) {
        String encodedToken = oAuth2Server.issueToken(
                ISSUER_TOKENX,
                fnr,
                "default",
                Map.of("pid", fnr)
        ).serialize();
        JwtToken jwtToken = new JwtToken(encodedToken);
        TokenValidationContext context = new TokenValidationContext(Map.of(ISSUER_TOKENX, jwtToken));
        contextHolder.setTokenValidationContext(context);

        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }

    private void værInnloggetMedAzureAD(Veileder veileder) {
        String subject = "tilfeldigebokstaver";
        String audience = "finn-kandidat-api";
        Map<String, String> claims = Map.of(
                "NAVident", veileder.getNavIdent(),
                "name", etFornavn() + " " + etEtternavn()
        );

        String encodedToken = oAuth2Server.issueToken(
                ISSUER_AZUREAD,
                "theclientid",
                new DefaultOAuth2TokenCallback(
                        ISSUER_AZUREAD,
                        subject,
                        "dummy",
                        Collections.singletonList(audience),
                        claims,
                        3600
                )
        ).serialize();

        JwtToken jwtToken = new JwtToken(encodedToken);
        TokenValidationContext context = new TokenValidationContext(Map.of(ISSUER_AZUREAD, jwtToken));
        contextHolder.setTokenValidationContext(context);

        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }

    private void værUinnlogget() {
        TokenValidationContext context = new TokenValidationContext(Collections.emptyMap());
        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }
}
