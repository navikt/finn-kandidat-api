package no.nav.finnkandidatapi.permittert;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.kandidat.AktorRegisteretUkjentFnrException;
import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.veilarboppfolging.VeilarbOppfolgingClient;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Protected
@RestController
public class SlettUtdatertePermitteringerController {

    private final PermittertArbeidssokerRepository permittertArbeidssokerRepository;
    private final VeilarbOppfolgingClient veilarbOppfolgingClient;
    private final AktørRegisterClient aktørRegisterClient;

    public SlettUtdatertePermitteringerController(
            PermittertArbeidssokerRepository permittertArbeidssokerRepository,
            VeilarbOppfolgingClient veilarbOppfolgingClient,
            AktørRegisterClient aktørRegisterClient
    ) {
        this.permittertArbeidssokerRepository = permittertArbeidssokerRepository;
        this.veilarbOppfolgingClient = veilarbOppfolgingClient;
        this.aktørRegisterClient = aktørRegisterClient;
    }

    @DeleteMapping("/permitteringer")
    public ResponseEntity slettAlleUtdatertePermitteringer() {
        List<PermittertArbeidssoker> arbeidssøkere = permittertArbeidssokerRepository.hentAllePermitterteArbeidssokere();
        log.info("Fant {} permitterte arbeidssøkere", arbeidssøkere.size());

        AtomicInteger antallSletta = new AtomicInteger();
        arbeidssøkere.forEach(permittertArbeidssoker -> {
            String fnr;
            try {
                fnr = aktørRegisterClient.tilFnr(permittertArbeidssoker.getAktørId());
            } catch (FinnKandidatException exception) {
                log.error("Kunne ikke hente fnr til bruker.", exception);
                return;
            }

            boolean erUnderOppfølging = veilarbOppfolgingClient.hentOppfølgingsstatus(fnr).isUnderOppfolging();
            if (!erUnderOppfølging) {
                Optional<Integer> id = permittertArbeidssokerRepository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());
                if (id.isPresent()) {
                    log.info("Slettet permittert arbeidssøker. Ny rad id: {}", id.get());
                    antallSletta.getAndIncrement();
                } else {
                    log.error("Greide ikke slette permittert arbeidssøker. aktørId: ");
                }
            }
        });

        log.info("Slettet {} permitterte arbeidssøkere ikke under oppfølging", antallSletta.get());
        return ResponseEntity.ok("Antall sletta: " + antallSletta.get());
    }
}
