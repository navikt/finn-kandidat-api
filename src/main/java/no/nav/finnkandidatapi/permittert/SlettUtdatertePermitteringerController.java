package no.nav.finnkandidatapi.permittert;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.veilarboppfolging.VeilarbOppfolgingClient;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Protected
@Controller
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
    public void slettAlleUtdatertePermitteringer() {
        List<PermittertArbeidssoker> arbeidssøkere = permittertArbeidssokerRepository.hentAllePermitterteArbeidssokere();
        log.info("Fant {} permitterte arbeidssøkere", arbeidssøkere.size());

        AtomicInteger antallSletta = new AtomicInteger();
        arbeidssøkere.forEach(permittertArbeidssoker -> {
            String fnr = aktørRegisterClient.tilFnr(permittertArbeidssoker.getAktørId());
            boolean erUnderOppfølging = veilarbOppfolgingClient.hentOppfølgingsstatus(fnr).isUnderOppfolging();
            if (!erUnderOppfølging) {
                Optional<Integer> id = permittertArbeidssokerRepository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());
                log.info("Slettet permittert arbeidssøker. Ny rad id: {}", id);
                antallSletta.getAndIncrement();
            }
        });

        log.info("Sletta {} permitterte arbeidssøkere ikke under oppfølging", antallSletta.get());
    }
}
