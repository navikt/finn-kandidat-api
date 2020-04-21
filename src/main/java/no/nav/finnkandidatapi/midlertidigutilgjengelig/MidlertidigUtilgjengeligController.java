package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.NotFoundException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Protected
@RestController
@RequestMapping("/midlertidig-utilgjengelig")
public class MidlertidigUtilgjengeligController {
    private final MidlertidigUtilgjengeligService service;
    private final TilgangskontrollService tilgangskontroll;

    public MidlertidigUtilgjengeligController(
            MidlertidigUtilgjengeligService service,
            TilgangskontrollService tilgangskontroll) {
        this.service = service;
        this.tilgangskontroll = tilgangskontroll;
    }

    private boolean datoErTilbakeITid(LocalDateTime tilDato) {
        LocalDate idag = LocalDate.now();
        LocalDateTime idagMidnatt = LocalDateTime.of(idag, LocalTime.MIDNIGHT);

        return tilDato.isBefore(idagMidnatt);
    }

    @GetMapping("/{aktørId}")
    public ResponseEntity<?> getMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId) {
        tilgangskontroll.hentInnloggetVeileder();

        Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig = service.hentMidlertidigUtilgjengelig(aktørId);

        return midlertidigUtilgjengelig.isEmpty() ?
             ResponseEntity.notFound().build() : ResponseEntity.ok(midlertidigUtilgjengelig.get());
    }

    @PostMapping
    public ResponseEntity<?> postMidlertidigUtilgjengelig(@RequestBody MidlertidigUtilgjengeligDto midlertidigUtilgjengelig) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        if (datoErTilbakeITid(midlertidigUtilgjengelig.getTilDato())) {
            return ResponseEntity.badRequest().body("Du kan ikke sette en kandidat som midlertidig utilgjengelig tilbake i tid");
        }

        if (service.midlertidigTilgjengeligEksisterer(midlertidigUtilgjengelig.getAktørId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Det er allerede registrert at kandidaten er midlertidig utilgjengelig");
        }

        Optional<MidlertidigUtilgjengelig> lagret = service.opprettMidlertidigUtilgjengelig(midlertidigUtilgjengelig, innloggetVeileder);
        if (lagret.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(lagret.get());
    }

    @PutMapping("/{aktørId}")
    public ResponseEntity<?> putMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId, @RequestBody MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        if (datoErTilbakeITid(midlertidigUtilgjengeligDto.getTilDato())) {
            return ResponseEntity.badRequest().body("Du kan ikke sette en kandidat som midlertidig utilgjengelig tilbake i tid");
        }

        if (!aktørId.equals(midlertidigUtilgjengeligDto.getAktørId())) {
            return ResponseEntity.badRequest().body("Aktør-id er annerledes i URL og body");
        }

        Optional<MidlertidigUtilgjengelig> endret = service.endreMidlertidigTilgjengelig(aktørId, midlertidigUtilgjengeligDto.getTilDato(), innloggetVeileder);

        return endret.isEmpty() ?
                ResponseEntity.notFound().build() : ResponseEntity.ok(endret.get());
    }

    @DeleteMapping("/{aktørId}")
    public ResponseEntity<MidlertidigUtilgjengelig> deleteMidlertidigUtilgjenglig(@PathVariable("aktørId") String aktørId) {
        tilgangskontroll.hentInnloggetVeileder();

        Integer antallOppdaterteRader = service.slettMidlertidigUtilgjengelig(aktørId);
        if (antallOppdaterteRader == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
