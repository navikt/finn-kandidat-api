package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.vedtak.Vedtak;

import java.time.LocalDateTime;
import java.util.Optional;

public class SjekkPermittertUtil {

    public static boolean sjekkOmErPermittert(Optional<PermittertArbeidssoker> permittertArbeidssoker, Optional<Vedtak> vedtak) {
        Optional<LocalDateTime> datoForVedtak = hentDatoForVedtak(vedtak);
        Optional<LocalDateTime> datoForVeilarbRegistrering = hentDatoForRegistrering(permittertArbeidssoker);

        if (harHverkenVedtakEllerRegistrering(datoForVedtak, datoForVeilarbRegistrering)) {
            return false;
        } else if (harVedtakMenIkkeRegistrering(datoForVedtak, datoForVeilarbRegistrering)) {
            return erVedtakGyldigOgForPermittering(vedtak.get());
        } else if (harRegistreringMenIkkeVedtak(datoForVedtak, datoForVeilarbRegistrering)) {
            return harArbeidssokerRegistrertSegSomPermittert(permittertArbeidssoker);
        } else {
            //har både vedtak og registrering
            if (datoForVeilarbRegistrering.get().isBefore(datoForVedtak.get())) {
                return erVedtakGyldigOgForPermittering(vedtak.get());
            } else {
                return harArbeidssokerRegistrertSegSomPermittert(permittertArbeidssoker);
            }
        }
    }

    static Optional<LocalDateTime> hentDatoForRegistrering(Optional<PermittertArbeidssoker> permittertArbeidssoker) {
        return permittertArbeidssoker.map(as -> as.getTidspunktForStatusFraVeilarbRegistrering());
    }

    static Optional<LocalDateTime> hentDatoForVedtak(Optional<Vedtak> vedtak) {
        return vedtak.map(v -> v.getFraDato());
    }

    static boolean harRegistreringMenIkkeVedtak(Optional<LocalDateTime> datoForSisteVedtak, Optional<LocalDateTime> datoForVeilarbRegistrering) {
        return datoForSisteVedtak.isEmpty() && datoForVeilarbRegistrering.isPresent();
    }

    static boolean harVedtakMenIkkeRegistrering(Optional<LocalDateTime> datoForSisteVedtak, Optional<LocalDateTime> datoForVeilarbRegistrering) {
        return datoForSisteVedtak.isPresent() && datoForVeilarbRegistrering.isEmpty();
    }

    static boolean harHverkenVedtakEllerRegistrering(Optional<LocalDateTime> datoForSisteVedtak, Optional<LocalDateTime> datoForVeilarbRegistrering) {
        return datoForSisteVedtak.isEmpty() && datoForVeilarbRegistrering.isEmpty();
    }

    static boolean harArbeidssokerRegistrertSegSomPermittert(Optional<PermittertArbeidssoker> permittertArbeidssoker) {
        return permittertArbeidssoker.get().erPermittert();
    }

    static boolean erVedtakGyldigOgForPermittering(Vedtak vedtak) {
        return vedtak.erGyldig() && vedtak.erPermittert();
    }
}
