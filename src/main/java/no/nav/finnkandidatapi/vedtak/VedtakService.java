package no.nav.finnkandidatapi.vedtak;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
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

    public void behandleVedtakReplikert(VedtakReplikert vedtakReplikert) {

        //Kan evt sjekke om senesteVedtak finnes og evt har nyere pos/timestamp, isåfall abort og evt log?
        //kan være aktuelt pga replay av gamle Kafka-meldinger..

        if (vedtakReplikert.getOp_type().equalsIgnoreCase("I" )) {
            behandleInsert(vedtakReplikert);

        } else if (vedtakReplikert.getOp_type().equalsIgnoreCase("U" )) {
            behandleUpdate(vedtakReplikert);

        } else if (vedtakReplikert.getOp_type().equalsIgnoreCase("D" )) {
            behandleDelete(vedtakReplikert);

        } else {
            log.warn("Ukjent operasjon {}", vedtakReplikert.getOp_type());
            if( vedtakReplikert.getAfter() != null ) {
                behandleUpdate(vedtakReplikert);
            } else if( vedtakReplikert.getBefore() != null ) {
                behandleDelete(vedtakReplikert);
            } else {
                log.error("Hverken before eller after er satt for {}", vedtakReplikert);
            }
        }
    }

    private void behandleDelete(VedtakReplikert vedtakReplikert) {
        //Vi vil neppe få disse, da vedtak ikke slettes i Arena og hvis de slettes så er det fra en tilstand som er filtrert bort fra oss
        log.info("Fikk en slettemelding!");

        if (!vedtakReplikert.getBefore().erPermitteringsvedtak()) {
            log.info("Dropper å lagre vedtak {}, da det ikke er et permitteringsvedtak", vedtakReplikert.getBefore().getVedtak_id());
            return;
        }

        String aktørId = hentAktørId(vedtakReplikert.getBefore().getFodselsnr());
        Vedtak vedtak = Vedtak.opprettFraBefore(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        int antallRader = vedtakRepository.logiskSlettVedtak(vedtak);
        if( antallRader < 1 ) {
            throw new RuntimeException("Sletting av vedtak feilet");
        }
        eventPublisher.publishEvent(new VedtakSlettet(vedtak));
        meterRegistry.counter(VEDTAK_SLETTET).increment();
    }

    private void behandleUpdate(VedtakReplikert vedtakReplikert) {
        if (!vedtakReplikert.getAfter().erPermitteringsvedtak()) {
            log.info("Dropper å lagre vedtak {}, da det ikke er et permitteringsvedtak", vedtakReplikert.getAfter().getVedtak_id());
            return;
        }

        String aktørId = hentAktørId(vedtakReplikert.getAfter().getFodselsnr());
        Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        eventPublisher.publishEvent(new VedtakEndret(vedtak));
        meterRegistry.counter(VEDTAK_LAGRET).increment();
    }

    private void behandleInsert(VedtakReplikert vedtakReplikert) {
        //Vi vil nok sjelden/aldri få Inserts, da vedtakene blir opprettet i Arena i en tilstand som blir filtrert vekk av GG.

        if (!vedtakReplikert.getAfter().erPermitteringsvedtak()) {
            log.info("Dropper å lagre vedtak {}, da det ikke er et permitteringsvedtak", vedtakReplikert.getAfter().getVedtak_id());
            return;
        }

        String aktørId = hentAktørId(vedtakReplikert.getAfter().getFodselsnr());
        Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
        Long id = vedtakRepository.lagreVedtak(vedtak);
        vedtak.setId(id);
        eventPublisher.publishEvent(new VedtakOpprettet(vedtak));
        meterRegistry.counter(VEDTAK_LAGRET).increment();
    }

    public String hentAktørId(String fnr) {
        return aktørRegisterClient.tilAktørId(fnr);
    }

    public Optional<Vedtak> hentNyesteVedtakForAktør(String aktørId) {
        return vedtakRepository.hentNyesteVersjonAvNyesteVedtakForAktør(aktørId);
    }
}
