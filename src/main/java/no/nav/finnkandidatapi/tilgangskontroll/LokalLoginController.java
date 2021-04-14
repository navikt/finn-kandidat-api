package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Profile("local")
@Unprotected
@RestController
@RequestMapping
public class LokalLoginController {

    private final MockOAuth2Server server;

    public LokalLoginController(MockOAuth2Server server) {
        this.server = server;
    }

    @GetMapping("/local/veileder-cookie")
    public void hentCookieMedVeilederJwtTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("isso-idtoken", veilederToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/local/ekstern-bruker-cookie")
    public void hentCookieMedEksternBrukerJwtTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("selvbetjening-idtoken", eksternBrukerToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/local/veileder-openam-cookie")
    public void hentCookieMedVeilederOpenAMJwtTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("ID_token", openAMToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String veilederToken(){
        String issuerId = "isso";
        String subject = "00000000000";
        String audience = "aud-isso";
        Map<String, String> claims = Map.of(
                "NAVident", "X123456",
                "given_name", "Ola",
                "family_name", "Nordmann"
        );

        return server.issueToken(
                issuerId,
                "theclientid",
                new DefaultOAuth2TokenCallback(
                        issuerId,
                        subject,
                        Collections.singletonList(audience),
                        claims,
                        3600
                )
        ).serialize();
    }

    private String eksternBrukerToken(){
        String issuerId = "selvbetjening";
        String subject = "28037639429";
        String audience = "aud-selvbetjening";
        return server.issueToken(
                issuerId,
                "theclientid",
                new DefaultOAuth2TokenCallback(
                        issuerId,
                        subject,
                        Collections.singletonList(audience),
                        Collections.emptyMap(),
                        3600
                )
        ).serialize();
    }

    private String openAMToken(){
        String issuerId = "openam";
        String subject = "X123456";
        String audience = "aud-openam";
        Map<String, String> claims = Map.of();

//        server.issueToken(issuerId, subject)

        return server.issueToken(
                issuerId,
                "theclientid",
                new DefaultOAuth2TokenCallback(
                        issuerId,
                        subject,
                        Collections.singletonList(audience),
                        claims,
                        3600
                )
        ).serialize();
    }
}
