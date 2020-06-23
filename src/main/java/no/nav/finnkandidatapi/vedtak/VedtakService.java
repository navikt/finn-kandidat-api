package no.nav.finnkandidatapi.vedtak;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
import no.nav.finnkandidatapi.kandidat.AktorRegisteretUkjentFnrException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class VedtakService {

    private static final String VEDTAK_LAGRET = "finnkandidat.vedtak.lagret";
    private static final String VEDTAK_SLETTET = "finnkandidat.vedtak.slettet";

    private final VedtakRepository vedtakRepository;
    private final AktørRegisterClient aktørRegisterClient;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public VedtakService(VedtakRepository vedtakRepository,
                         AktørRegisterClient aktørRegisterClient,
                         ApplicationEventPublisher eventPublisher,
                         MeterRegistry meterRegistry) {
        this.vedtakRepository = vedtakRepository;
        this.aktørRegisterClient = aktørRegisterClient;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    public Optional<Vedtak> hentNyesteVedtakForAktør(String aktørId) {
        return vedtakRepository.hentNyesteVersjonAvNyesteVedtakForAktør(aktørId);
    }

    public void behandleVedtakReplikert(VedtakReplikert vedtakReplikert) {
        //Kan evt sjekke om senesteVedtak finnes og evt har nyere pos/timestamp, isåfall abort og evt log?
        //kan være aktuelt pga replay av gamle Kafka-meldinger..

        String aktørId = hentAktørId(vedtakReplikert);
        if (aktørId == null) {
            return;
        }

        if (vedtakReplikert.getOp_type().equalsIgnoreCase("I")) {
            behandleInsert(vedtakReplikert, aktørId);

        } else if (vedtakReplikert.getOp_type().equalsIgnoreCase("U")) {
            behandleUpdate(vedtakReplikert, aktørId);

        } else if (vedtakReplikert.getOp_type().equalsIgnoreCase("D")) {
            behandleDelete(vedtakReplikert, aktørId);

        } else {
            log.warn("Ukjent operasjon {}", vedtakReplikert.getOp_type());
            if (vedtakReplikert.getAfter() != null) {
                behandleUpdate(vedtakReplikert, aktørId);
            } else if (vedtakReplikert.getBefore() != null) {
                behandleDelete(vedtakReplikert, aktørId);
            } else {
                log.error("Hverken before eller after er satt for {}", vedtakReplikert);
            }
        }
    }

    private void behandleDelete(VedtakReplikert vedtakReplikert, String aktørId) {
        //Vi vil neppe få disse, da vedtak ikke slettes i Arena og hvis de slettes så er det fra en tilstand som er filtrert bort fra oss
        log.info("Fikk en slettemelding!");

        Vedtak vedtak = Vedtak.opprettFraBefore(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        int antallRader = vedtakRepository.logiskSlettVedtak(vedtak);
        if (antallRader < 1) {
            throw new RuntimeException("Sletting av vedtak feilet");
        }
        eventPublisher.publishEvent(new VedtakSlettet(vedtak));
        meterRegistry.counter(VEDTAK_SLETTET).increment();
    }

    private void behandleUpdate(VedtakReplikert vedtakReplikert, String aktørId) {
        log.info("Fikk en updatemelding!");

        Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        eventPublisher.publishEvent(new VedtakEndret(vedtak));
        meterRegistry.counter(VEDTAK_LAGRET).increment();
    }

    private void behandleInsert(VedtakReplikert vedtakReplikert, String aktørId) {
        //Vi vil nok sjelden/aldri få Inserts, da vedtakene blir opprettet i Arena i en tilstand som blir filtrert vekk av GG.
        log.info("Fikk en insertmelding!");

        Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        eventPublisher.publishEvent(new VedtakOpprettet(vedtak));
        meterRegistry.counter(VEDTAK_LAGRET).increment();
    }

    private String hentAktørId(VedtakReplikert vedtakReplikert) {
        String fodselsnr = vedtakReplikert.getTokens().getFodselsnr();
        if (fodselsnr == null || fodselsnr.isEmpty()) {
            log.error("Ingen fødselsnummer i Vedtak replikert meldingen, dropper videre behandling. {}", vedtakReplikert);
            return null;
        }
        try {
            return hentAktørId(fodselsnr);
        } catch (AktorRegisteretUkjentFnrException e) {
            log.error("Funksjonell feil mot aktørregisteret, dropper videre behandling", e);
            return null;
        }
    }

    private String hentAktørId(String fnr) {
        return aktørRegisterClient.tilAktørId(fnr);
    }
}
