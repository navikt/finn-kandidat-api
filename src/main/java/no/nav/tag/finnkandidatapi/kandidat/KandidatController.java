package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.Kjetil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Protected
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
public class KandidatController extends AbstractAggregateRoot<KandidatController> {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final KandidatService kandidatService;
    private final TokenUtils tokenUtils;

    @GetMapping("/{fnr}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("fnr") String fnr) {
        Kandidat kandidat = kandidatService.hentNyesteKandidat(fnr).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        List<Kandidat> kandidater = kandidatService.hentKandidater();
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> lagreKandidat(@RequestBody Kandidat kandidat) {
        Veileder veileder = tokenUtils.hentInnloggetVeileder();
        Kandidat lagretKandidat = kandidatService.lagreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        applicationEventPublisher.publishEvent(lagretKandidat);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lagretKandidat);
    }
}
