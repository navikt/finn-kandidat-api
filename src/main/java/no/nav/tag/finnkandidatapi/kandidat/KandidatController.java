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
    public ResponseEntity<Kandidat> hentKandidat() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Kandidat> lagreKandidat(Kandidat kandidat) {
        Veileder veileder = tokenUtils.hentInnloggetVeileder();
        kandidatService.oppdaterKandidat(kandidat, veileder);

        Integer id = kandidatRepository.lagreKandidat(kandidat);
        // TODO: Bruk fnr
        Kandidat lagretKandidat = kandidatRepository.hentKandidat(id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lagretKandidat);
    }
}
