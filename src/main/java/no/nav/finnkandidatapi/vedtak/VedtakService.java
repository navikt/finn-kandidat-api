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
    private static final String VEDTAK_UPDATE_LAGRET = "finnkandidat.vedtak.insert.lagret";
    private static final String VEDTAK_DELETE_LAGRET = "finnkandidat.vedtak.insert.lagret";

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

    // TODO: Alternativ måte å gjøre det på, slett hvis uaktuellt..
    public void behandleVedtakEndretIdempotent(VedtakReplikert vedtakEndret) {

        if ( vedtakEndret.getOp_type().equalsIgnoreCase("I")) {
            behandleInsert(vedtakEndret.getAfter());
        } else if( vedtakEndret.getOp_type().equalsIgnoreCase("D")) {
            //Slettes noen gang vedtak i Arena?
            behandleDelete(vedtakEndret.getBefore());
        } else if( vedtakEndret.getOp_type().equalsIgnoreCase("U")) {
            behandleUpdate(vedtakEndret.getBefore(), vedtakEndret.getAfter());
        }
    }

    private void behandleInsert(VedtakRad after) {
        //sjekk om vedtak finnes (det kan finnes pga replay fra Kafka)
        //Hvis ja, logg en warning, ikke overskriv
        //hvis vedtaket ikke finnes, lagre det
    }

    private void behandleDelete(VedtakRad before) {
        //hvis vedtak finnes, marker det som slettet
        //hvis det ikke finnes, logg en warning (Dette bør egentlig ikke skje..)
    }

    private void behandleUpdate(VedtakRad before, VedtakRad after) {
        //hvis before er ugyldig og after er gyldig, er dette det samme som en insert for oss.
        //Tidligere operasjoner på dette vedtaket i Arena har ikke blitt replikert over..
        //kall behandleInsert(after)

        //hvis before er gyldig og after er ugyldig, er dette det samme som en delete for oss.
        //Vedtaket går over i en tilstand i Arena hvor vi ikke vil få videre replikering
        //kall behandleDelete(before)

        //hvis before er gyldig og after er gyldig, så er dette en reell update.
        //sjekk om vedtak finnes. Hvis nei, logg en Warning og lagre det
        //hvis ja, oppdater verdiene i vedtaket. Fjern evt. slettet-markering

        //hvis before er ugyldig og after er ugyldig, skulle vi ikke fått en melding.
        //Enten har vi misforstått noe, eller så er det noe galt i GG. Logg en Error.
        //Sjekk om vedtaket evt finnes, hvis ja marker som slettet.
    }






}
