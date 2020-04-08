package no.nav.finnkandidatapi.permittert;

import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.veilarboppfolging.VeilarbOppfolgingClient;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

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
        arbeidssøkere.forEach(permittertArbeidssoker -> {
            String fnr = aktørRegisterClient.tilFnr(permittertArbeidssoker.getAktørId());
            boolean erUnderOppfølging = veilarbOppfolgingClient.hentOppfølgingsstatus(fnr).isUnderOppfolging();
            if (erUnderOppfølging) {
                permittertArbeidssokerRepository.slettPermittertArbeidssoker(permittertArbeidssoker.getAktørId());
            }
        });
    }
}
