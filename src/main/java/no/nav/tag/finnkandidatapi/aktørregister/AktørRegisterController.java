package no.nav.tag.finnkandidatapi.aktørregister;

import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aktørregister")
@RequiredArgsConstructor
@Protected
public class AktørRegisterController {

    private final AktørRegisterClient client;

    // TODO: Fjern
    @GetMapping("/{aktørId}")
    public ResponseEntity<String> testAktørregister(@PathVariable("aktørId") String aktørId) {
        return ResponseEntity.ok(client.tilFnr(aktørId));
    }
}
