package no.nav.tag.finnkandidatapi.kandidat;


import no.nav.security.oidc.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController("/kandidater")
public class KandidatController {

    private final KandidatRepository kandidatRepository;
    private final KandidatService kandidatService;
    private final TokenUtils tokenUtils;

    @Autowired
    public KandidatController(KandidatRepository kandidatRepository, KandidatService kandidatService, TokenUtils tokenUtils) {
        this.kandidatRepository = kandidatRepository;
        this.kandidatService = kandidatService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping
    public ResponseEntity<Kandidat> hentKandidat(String fnr) {
        Kandidat kandidat = kandidatRepository.hentNyesteKandidat(fnr).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @PostMapping
    public ResponseEntity<Kandidat> lagreKandidat(Kandidat kandidat) {
        Veileder veileder = tokenUtils.hentInnloggetVeileder();
        kandidatService.oppdaterKandidat(kandidat, veileder);

        Integer id = kandidatRepository.lagreKandidat(kandidat);
        Kandidat lagretKandidat = kandidatRepository.hentKandidat(id).orElseThrow(NotFoundException::new);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lagretKandidat);
    }
}
