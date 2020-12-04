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

import java.util.Map;

@Profile("local")
@RestController
@RequestMapping
public class LokalLoginController {

    private final MockOAuth2Server server;

    public LokalLoginController(MockOAuth2Server server) {
        this.server = server;
    }

    @Unprotected
    @GetMapping("/local/veileder-cookie")
    public void hentCookieMedVeilederJwtTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("isso-idtoken", token());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String token(){
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
                        audience,
                        claims,
                        3600
                )
        ).serialize();
    }
}
