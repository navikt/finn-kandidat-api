package no.nav.finnkandidatapi.vedtak;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.utils.graphql.GraphqlErrorException;
import no.nav.common.types.identer.Fnr;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakRad;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static no.nav.finnkandidatapi.SecureLog.secureLog;

@Slf4j
@Service
public class VedtakService {

    private final VedtakRepository vedtakRepository;
    private final AktorOppslagClient aktorOppslagClient;
    private final ApplicationEventPublisher eventPublisher;

    public VedtakService(
            VedtakRepository vedtakRepository,
            AktorOppslagClient aktorOppslagClient,
            ApplicationEventPublisher eventPublisher
    ) {
        this.vedtakRepository = vedtakRepository;
        this.aktorOppslagClient = aktorOppslagClient;
        this.eventPublisher = eventPublisher;
    }

    public Optional<Vedtak> hentNyesteVedtakForAktør(String aktørId) {
        Optional<Vedtak> nyesterad = vedtakRepository.hentNyesteVedtakForAktør(aktørId);
        if(nyesterad.stream().anyMatch(it -> "AVSLU".equals(it.getStatusKode()))) {
            return nyesterad;
        }
        return vedtakRepository.hentNyesteVersjonAvNyesteVedtakForAktør(aktørId);
    }

    public void behandleVedtakReplikert(VedtakReplikert vedtakReplikert) {
        String aktørId = hentAktørId(vedtakReplikert);
        if (aktørId == null) {
            return;
        }
        log.info("Mottok vedtak med aktørId se securelog for aktørId");
        secureLog.info("Mottok vedtak med aktørId " + aktørId);

        if (vedtakReplikert.getOp_type().equalsIgnoreCase("I") && erVedtakGodkjent(vedtakReplikert.getAfter())) {
            behandleInsert(vedtakReplikert, aktørId);
        } else if (vedtakReplikert.getOp_type().equalsIgnoreCase("U") && erVedtakGodkjent(vedtakReplikert.getAfter())) {
            behandleUpdate(vedtakReplikert, aktørId);
        } else if (vedtakReplikert.getOp_type().equalsIgnoreCase("D") && erVedtakGodkjent(vedtakReplikert.getBefore())) {
            behandleDelete(vedtakReplikert, aktørId);
        } else {
            log.info("Mottatt  melding som vi forkaster opType:{} before:{} after:{}", vedtakReplikert.getOp_type(), vedtakReplikert.getBefore(), vedtakReplikert.getAfter());
        }
    }

    private void behandleDelete(VedtakReplikert vedtakReplikert, String aktørId) {
        // Vi vil neppe få disse, da vedtak ikke slettes i Arena og hvis de slettes så er det fra en tilstand som er filtrert bort fra oss

        Vedtak vedtak = Vedtak.opprettFraBefore(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        int antallRader = vedtakRepository.logiskSlettVedtak(vedtak);
        if (antallRader < 1) {
            throw new RuntimeException("Sletting av vedtak feilet");
        }
        eventPublisher.publishEvent(new VedtakSlettet(vedtak));
    }

    private void behandleUpdate(VedtakReplikert vedtakReplikert, String aktørId) {
        Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        eventPublisher.publishEvent(new VedtakEndret(vedtak));
    }

    private void behandleInsert(VedtakReplikert vedtakReplikert, String aktørId) {
        //Vi vil nok sjelden/aldri få Inserts, da vedtakene blir opprettet i Arena i en tilstand som blir filtrert vekk av GG.

        Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        eventPublisher.publishEvent(new VedtakOpprettet(vedtak));
    }

    private String hentAktørId(VedtakReplikert vedtakReplikert) {
        String fodselsnr = vedtakReplikert.getFodselsnr();

        if (fodselsnr == null || fodselsnr.isEmpty()) {
            log.error("Ingen fødselsnummer i Vedtak replikert meldingen, dropper videre behandling. {}", vedtakReplikert);
            return null;
        }

        try {
            return hentAktørId(fodselsnr);
        } catch (GraphqlErrorException e) {
            boolean fantIkkePersonIPdl = e.getErrors().stream().anyMatch(error -> error.getExtensions().getCode().equals("not_found"));
            String vedtakId = vedtakReplikert.getAfter().getVedtak_id().toString();

            if (fantIkkePersonIPdl) {
                log.error("Fant ikke person i PDL fra vedtak replikert-melding med ID " + vedtakId + ". Ignorerer melding.", e);
                return null;
            } else {
                throw e;
            }
        }
    }

    private String hentAktørId(String fnr) {
        return aktorOppslagClient.hentAktorId(new Fnr(fnr)).get();
    }

    private boolean erVedtakGodkjent(VedtakRad vedtakRad) {
        return vedtakRad.getUtfallkode() != null && vedtakRad.getUtfallkode().equals("JA");
    }
}
