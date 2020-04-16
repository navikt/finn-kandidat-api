package no.nav.finnkandidatapi.midlertidigUtilgjengelig;

import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MidlertidigUtilgjengeligService {

    public MidlertidigUtilgjengelig hentMidlertidigUtilgjengelig(String aktørId, Veileder innloggetVeileder) {
        // TODO: Snakk med repository
        return MidlertidigUtilgjengelig.builder()
                .id(1)
                .aktørId("0000000000")
                .fraDato(LocalDateTime.now())
                .tilDato(LocalDateTime.now().plusDays(15))
                .registreringstidspunkt(LocalDateTime.now())
                .registrertAv(innloggetVeileder.getNavIdent())
                .build();
    }

    public MidlertidigUtilgjengelig lagreMidlertidigUtilgjengelig(MidlertidigUtilgjengeligDto midlertidigUtilgjengelig, Veileder innloggetVeileder) {
        // TODO: Snakk med repository
        return MidlertidigUtilgjengelig.builder()
                .id(1)
                .aktørId(midlertidigUtilgjengelig.getAktørId())
                .fraDato(midlertidigUtilgjengelig.getFraDato())
                .tilDato(midlertidigUtilgjengelig.getTilDato())
                .registreringstidspunkt(LocalDateTime.now())
                .registrertAv(innloggetVeileder.getNavIdent())
                .build();
    }
}
