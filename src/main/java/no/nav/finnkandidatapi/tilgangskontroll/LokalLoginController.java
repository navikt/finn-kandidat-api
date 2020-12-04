package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/jejjo")
public class LokalLoginController {

    private final MockOAuth2Server server;

    public LokalLoginController(MockOAuth2Server server) {
        this.server = server;
    }

    @Unprotected
    @GetMapping("/egenmekka-cookie")
    public void hentCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("isso-idtoken", token());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String token(){
        String issuerId = "isso";
        String subject = "123456";
        String audience = "aud-isso";

        return server.issueToken(
                issuerId,
                "theclientid",
                new DefaultOAuth2TokenCallback(
                        issuerId,
                        subject,
                        audience,
                        Map.of(
                                "NAVident", "Y123456",
                                "given_name", "Ola",
                                "family_name", "Nordmann"
                        ),
                        3600
                )
        ).serialize();
    }
}
