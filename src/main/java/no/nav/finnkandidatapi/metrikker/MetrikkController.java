package no.nav.finnkandidatapi.metrikker;

import lombok.RequiredArgsConstructor;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Component
@Protected
@RestController
@RequiredArgsConstructor
public class MetrikkController {
    private final KandidatRepository kandidatRepository;

    @GetMapping("/internal/metrikker/navkontor")
    public ResponseEntity<String> hentAndelKandidaterMedNavkontor() {
        List<Kandidat> kandidater = kandidatRepository.hentKandidater();

        int antallKandidater = kandidater.size();
        int antallMedNavkontor = kandidater.stream()
                .filter(kandidat -> kandidat.getNavKontor() != null)
                .toArray().length;

        String respons = antallMedNavkontor + " av " + antallKandidater + " kandidater har NAV-kontor";
        return ResponseEntity.ok(respons);
    }
}
