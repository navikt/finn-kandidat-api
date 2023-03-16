package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.vedtak.Vedtak;

import java.time.LocalDateTime;
import java.util.Optional;

import static no.nav.finnkandidatapi.SecureLog.secureLog;


@Slf4j
public class SjekkPermittertUtil {

    public static boolean sjekkOmErPermittert(Optional<PermittertArbeidssoker> permittertArbeidssoker, Optional<Vedtak> vedtak) {
        Optional<LocalDateTime> datoForVedtak = hentDatoForVedtak(vedtak);
        Optional<LocalDateTime> datoForVeilarbRegistrering = hentDatoForRegistrering(permittertArbeidssoker);
        String aktørid = vedtak.map(it -> it.getAktørId()).orElse("");

        if (harHverkenVedtakEllerRegistrering(datoForVedtak, datoForVeilarbRegistrering)) {
            log.info("aktør har hverken vedtak eller registrering, se securelog for aktørId");
            secureLog.info(aktørid + " har hverken vedtak eller registrering");
            return false;
        } else if (harVedtakMenIkkeRegistrering(datoForVedtak, datoForVeilarbRegistrering)) {
            log.info("aktør har vedtak, se securelog for aktørId");
            secureLog.info(aktørid + " har vedtak");
            return erVedtakGyldigOgForPermittering(vedtak.get());
        } else if (harRegistreringMenIkkeVedtak(datoForVedtak, datoForVeilarbRegistrering)) {
            log.info("aktør har registrering, se securelog for aktørId");
            secureLog.info(aktørid + " har registrering");
            return harArbeidssokerRegistrertSegSomPermittert(permittertArbeidssoker);
        } else {
            log.info("aktør har både vedtak og registrering, se securelog for aktørId");
            secureLog.info(aktørid + " har både vedtak og registrering");
            //har både vedtak og registrering
            if (datoForVeilarbRegistrering.get().isBefore(datoForVedtak.get())) {
                log.info("aktør har permitteringsdato før vedtaksdato, se securelog for aktørId");
                secureLog.info(aktørid + " har permitteringsdato før vedtaksdato");
                return erVedtakGyldigOgForPermittering(vedtak.get());
            } else {
                log.info("aktør har ikke permitteringsdato før vedtaksdato, se securelog for aktørId");
                secureLog.info(aktørid + " har ikke permitteringsdato før vedtaksdato");
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
        log.info("Sjekker arbeidssøkers registrering for registrering som permitert, se securelog for aktørId");
        secureLog.info("Sjekker arbeidssøkers registrering for registrering som permitert for aktørId: " + permittertArbeidssoker.get().getAktørId() + ", er permitert: " + permittertArbeidssoker.get().erPermittert());
        return permittertArbeidssoker.get().erPermittert();
    }

    static boolean erVedtakGyldigOgForPermittering(Vedtak vedtak) {
        log.info("Sjekker gyldighet av vedtak og om permittert for aktør, se securelog for aktørId");
        secureLog.info("Sjekker gyldighet av vedtak " + vedtak.erGyldig() + " " + vedtak.erPermittert() + " for aktørId :" + vedtak.getAktørId());
        return vedtak.erGyldig() && vedtak.erPermittert();
    }
}
