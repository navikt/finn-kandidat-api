package no.nav.tag.finnkandidatapi;

import no.nav.security.oidc.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/hello-world")
    @Protected
    public String helloWorld() {
        return "Hallo fra backend!";
    }
}
