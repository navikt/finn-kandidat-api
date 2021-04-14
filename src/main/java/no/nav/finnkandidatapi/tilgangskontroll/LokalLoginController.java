package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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

    private String veilederToken(){
        String issuerId = "isso";
        String subject = "ikke-i-bruk";
        String audience = "default";
        Map<String, String> claims = Map.of(
                "NAVident", "X123456",
                "given_name", "Ola",
                "family_name", "Nordmann"
        );

        return server.issueToken(issuerId, subject, audience, claims).serialize();
    }

    @GetMapping("/local/ekstern-bruker-cookie")
    public void hentCookieMedEksternBrukerJwtTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("selvbetjening-idtoken", eksternBrukerToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String eksternBrukerToken(){
        String issuerId = "selvbetjening";
        String subject = "28037639429";
        return server.issueToken(issuerId, subject).serialize();
    }

    @GetMapping("/local/veileder-openam-cookie")
    public void hentCookieMedVeilederOpenAMJwtTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("ID_token", openAMToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String openAMToken(){
        String issuerId = "openam";
        String subject = "X123456";
        return server.issueToken(issuerId, subject).serialize();
    }
}
