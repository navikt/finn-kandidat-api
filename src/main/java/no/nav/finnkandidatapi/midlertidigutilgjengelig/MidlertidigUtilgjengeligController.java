package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_ISSO;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.*;

@ProtectedWithClaims(issuer = ISSUER_ISSO)
@RestController
@RequestMapping("/midlertidig-utilgjengelig")
@Slf4j
public class MidlertidigUtilgjengeligController {
    private final MidlertidigUtilgjengeligService service;
    private final TilgangskontrollService tilgangskontroll;

    private final String DATO_TILBAKE_I_TID_FEIL = "Du kan ikke sette en kandidat som midlertidig utilgjengelig tilbake i tid";
    private final String DATO_MER_ENN_30_DAGER_FREM_I_TID_FEIL = "Du kan ikke sette en kandidat som midlertidig utilgjengelig mer enn 30 dager frem i tid";

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

    private boolean datoErMerEnn30DagerFremITid(LocalDateTime tilDato) {
        LocalDate idag = LocalDate.now();
        LocalDateTime idagMidnatt = LocalDateTime.of(idag, LocalTime.MIDNIGHT);

        return !tilDato.isBefore(idagMidnatt.plusDays(31));
    }

    @GetMapping("/{aktørId}")
    public ResponseEntity<?> getMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId) {
        tilgangskontroll.hentInnloggetVeileder();
        MidlertidigUtilgjengeligOutboundDto dto = service.hentMidlertidigUtilgjengelig(aktørId).
                map(MidlertidigUtilgjengeligOutboundDto::new).
                orElse(new MidlertidigUtilgjengeligOutboundDto(null));
        return ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> postMidlertidigUtilgjengelig(@RequestBody MidlertidigUtilgjengeligInboundDto inbound) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        log.info("Midlertidig utilgjengelig med aktør {} opprettes av {}", inbound.getAktørId(), innloggetVeileder);

        if (datoErTilbakeITid(inbound.getTilDato())) {
            return ResponseEntity.badRequest().body(DATO_TILBAKE_I_TID_FEIL);
        }

        if (datoErMerEnn30DagerFremITid(inbound.getTilDato())) {
            return ResponseEntity.badRequest().body(DATO_MER_ENN_30_DAGER_FREM_I_TID_FEIL);
        }

        if (service.midlertidigTilgjengeligEksisterer(inbound.getAktørId())) {
            return status(HttpStatus.CONFLICT).body("Det er allerede registrert at kandidaten er midlertidig utilgjengelig");
        }

        return service.opprettMidlertidigUtilgjengelig(inbound, innloggetVeileder).
                map(MidlertidigUtilgjengeligOutboundDto::new).
                map(outbound -> status(CREATED).body(outbound)).
                orElse(status(INTERNAL_SERVER_ERROR).build());

    }

    @PutMapping("/{aktørId}")
    public ResponseEntity<?> putMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId, @RequestBody MidlertidigUtilgjengeligInboundDto dto) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        log.info("Midlertidig utilgjengelig med aktør {} oppdateres av {}", aktørId, innloggetVeileder);

        if (datoErTilbakeITid(dto.getTilDato())) {
            return ResponseEntity.badRequest().body(DATO_TILBAKE_I_TID_FEIL);
        }

        if (datoErMerEnn30DagerFremITid(dto.getTilDato())) {
            return ResponseEntity.badRequest().body(DATO_MER_ENN_30_DAGER_FREM_I_TID_FEIL);
        }

        if (!aktørId.equals(dto.getAktørId())) {
            return ResponseEntity.badRequest().body("Aktør-id er annerledes i URL og body");
        }

        return service.endreMidlertidigTilgjengelig(aktørId, dto.getTilDato(), innloggetVeileder).
                map(MidlertidigUtilgjengeligOutboundDto::new).
                map(ResponseEntity::ok).
                orElse(notFound().build());
    }

    @DeleteMapping("/{aktørId}")
    public ResponseEntity<MidlertidigUtilgjengelig> deleteMidlertidigUtilgjenglig(@PathVariable("aktørId") String aktørId) {

        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();
        log.info("Midlertidig utilgjengelig med aktør {} slettes av {}", aktørId, innloggetVeileder);

        Integer antallOppdaterteRader = service.slettMidlertidigUtilgjengelig(aktørId, innloggetVeileder);
        if (antallOppdaterteRader == 0) {
            return notFound().build();
        }

        return ok().build();
    }
}