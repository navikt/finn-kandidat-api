package no.nav.finnkandidatapi.vedtak;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakRad;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class VedtakService {

    private static final String VEDTAK_INSERT_LAGRET = "finnkandidat.vedtak.insert.lagret";
    private static final String VEDTAK_UPDATE_LAGRET = "finnkandidat.vedtak.update.lagret";
    private static final String VEDTAK_DELETE_LAGRET = "finnkandidat.vedtak.delete.lagret";

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

        //evt sjekk om senesteVedtak finnes og evt har nyere pos/timestamp, isåfall abort og evt log?
        //kan være aktuelt pga replay av gamle Kafka-meldinger..

        //TODO: Kan et vedtak endre rettighetKode, eller vil det være likt fra det opprettes til det evt slettes?
        //Hvis det ikke kan endres, så kan vi filtrere bort det som ikke er FISK eller PERM..

        if ( vedtakReplikert.getOp_type().equalsIgnoreCase("I")) {
            String aktørId = hentAktørId(vedtakReplikert.getAfter().getFodselsnr());
            Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
            Long id = vedtakRepository.lagreVedtak(vedtak);
            vedtak.setId(id);
            eventPublisher.publishEvent(new VedtakOpprettet(vedtak));
            meterRegistry.counter(VEDTAK_INSERT_LAGRET).increment();

        } else if( vedtakReplikert.getOp_type().equalsIgnoreCase("U")) {
            String aktørId = hentAktørId(vedtakReplikert.getAfter().getFodselsnr());
            Vedtak vedtak = Vedtak.opprettFraAfter(aktørId, vedtakReplikert);
            Long id = vedtakRepository.lagreVedtak(vedtak);
            vedtak.setId(id);
            eventPublisher.publishEvent(new VedtakEndret(vedtak));
            meterRegistry.counter(VEDTAK_UPDATE_LAGRET).increment();

        } else if( vedtakReplikert.getOp_type().equalsIgnoreCase("D")) {
            //Slettes noen gang vedtak i Arena?
            String aktørId = hentAktørId(vedtakReplikert.getBefore().getFodselsnr());
            Vedtak vedtak = Vedtak.opprettFraBefore(aktørId, vedtakReplikert);
            Long id = vedtakRepository.lagreVedtak(vedtak);
            vedtak.setId(id);
            eventPublisher.publishEvent(new VedtakSlettet(vedtak));
            meterRegistry.counter(VEDTAK_DELETE_LAGRET).increment();
        } else {
            log.error("Ukjent operasjon {}", vedtakReplikert.getOp_type());
        }
    }

    public String hentAktørId(String fnr) {
        return aktørRegisterClient.tilAktørId(fnr);
    }

    public List<Vedtak> hentNyesteVedtakForAktør(String aktørId) {
        return vedtakRepository.hentNyesteVedtakForAktør(aktørId);
    }
}
