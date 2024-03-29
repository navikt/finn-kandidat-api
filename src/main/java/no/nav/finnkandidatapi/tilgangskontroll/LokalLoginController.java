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
    public void hentCookieMedVeilederAzureAdTokenClaims(HttpServletResponse response) {
        Cookie cookie = new Cookie("azuread", veilederToken());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String veilederToken(){
        String issuerId = "azuread";
        String subject = "ikke-i-bruk";
        String audience = "finn-kandidat-api";
        Map<String, String> claims = Map.of(
                "NAVident", "X123456",
                "name", "Ola Nordmann",
                "unique_name", "ola.nordmann@nav.no"
        );

        return server.issueToken(issuerId, subject, audience, claims).serialize();
    }
}
