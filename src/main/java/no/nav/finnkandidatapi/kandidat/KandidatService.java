package no.nav.finnkandidatapi.kandidat;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.finnkandidatapi.veilarbarena.Oppfølgingsbruker;
import no.nav.finnkandidatapi.veilarbarena.VeilarbArenaClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KandidatService {

    private static final String ENDRET_OPPFØLGING_OPPDATERTE_NAVKONTOR = "finnkandidat.endretoppfolging.oppdaterte.navkontor";

    private final KandidatRepository kandidatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AktørRegisterClient aktørRegisterClient;
    private final DateProvider dateProvider;
    private final VeilarbArenaClient veilarbarenaClient;
    private final MeterRegistry meterRegistry;

    public KandidatService(
            KandidatRepository kandidatRepository,
            ApplicationEventPublisher eventPublisher,
            AktørRegisterClient aktørRegisterClient,
            DateProvider dateProvider,
            VeilarbArenaClient veilarbarenaClient,
            MeterRegistry meterRegistry
    ) {
        this.kandidatRepository = kandidatRepository;
        this.eventPublisher = eventPublisher;
        this.aktørRegisterClient = aktørRegisterClient;
        this.dateProvider = dateProvider;
        this.veilarbarenaClient = veilarbarenaClient;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(ENDRET_OPPFØLGING_OPPDATERTE_NAVKONTOR);
    }

    public Optional<Kandidat> hentNyesteKandidat(String aktørId) {
        return kandidatRepository.hentNyesteKandidat(aktørId);
    }

    public List<Kandidat> hentKandidater() {
        return kandidatRepository.hentKandidater();
    }

    public Optional<Kandidat> opprettKandidat(KandidatDto kandidat, Veileder innloggetVeileder) {
        Oppfølgingsbruker oppfølgingsbruker = veilarbarenaClient.hentOppfølgingsbruker(kandidat.getFnr(), kandidat.getAktørId());
        String navKontor = oppfølgingsbruker.getNavKontor();

        Kandidat kandidatTilLagring = Kandidat.opprettKandidat(
                kandidat,
                innloggetVeileder,
                dateProvider.now(),
                navKontor
        );

        Integer databaseId = kandidatRepository.lagreKandidatSomVeileder(kandidatTilLagring);
        Optional<Kandidat> lagretKandidat = kandidatRepository.hentKandidat(databaseId);
        lagretKandidat.ifPresent(value -> eventPublisher.publishEvent(new KandidatOpprettet(value)));

        return lagretKandidat;
    }

    public Optional<Kandidat> endreKandidat(KandidatDto kandidatDto, Veileder innloggetVeileder) {
        Optional<Kandidat> nyesteKandidat = hentNyesteKandidat(kandidatDto.getAktørId());
        if (nyesteKandidat.isEmpty()) return Optional.empty();

        Kandidat endretkandidat = Kandidat.endreKandidat(
                nyesteKandidat.get(),
                kandidatDto,
                innloggetVeileder,
                dateProvider.now()
        );

        Integer id = kandidatRepository.lagreKandidatSomVeileder(endretkandidat);
        Optional<Kandidat> lagretKandidat = kandidatRepository.hentKandidat(id);
        lagretKandidat.ifPresent(value -> eventPublisher.publishEvent(new KandidatEndret(value)));

        return lagretKandidat;
    }

    public void behandleOppfølgingEndret(Oppfølgingsbruker oppfølgingEndretMelding) {
        int antallOppdaterteRader = kandidatRepository.oppdaterNavKontor(oppfølgingEndretMelding.getFnr(), oppfølgingEndretMelding.getNavKontor());
        if (antallOppdaterteRader > 0) {
            log.info("Oppdaterte NAV-kontor på {} rader.", antallOppdaterteRader);
            meterRegistry.counter(ENDRET_OPPFØLGING_OPPDATERTE_NAVKONTOR).increment(antallOppdaterteRader);
        } else {
            log.info("Oppfølging endret-melding gjaldt ingen av systemets kandidater. Ignorerer.");
        }
    }

    public void behandleOppfølgingAvsluttet(OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding) {
        Optional<Integer> slettetKey = kandidatRepository.slettKandidatSomMaskinbruker(oppfølgingAvsluttetMelding.getAktørId(), dateProvider.now());
        if (slettetKey.isPresent()) {
            eventPublisher.publishEvent(new KandidatSlettet(slettetKey.get(), oppfølgingAvsluttetMelding.getAktørId(), Brukertype.SYSTEM, dateProvider.now()));
            log.info("Slettet kandidat med id {} pga. avsluttet oppfølging", slettetKey.get());
        }
    }

    public String hentAktørId(String fnr) {
        return aktørRegisterClient.tilAktørId(fnr);
    }

    public String hentFnr(String aktørId) {
        return aktørRegisterClient.tilFnr(aktørId);
    }

    Optional<Integer> slettKandidat(String aktørId, Veileder innloggetVeileder) {
        LocalDateTime slettetTidspunkt = dateProvider.now();
        Optional<Integer> optionalId = kandidatRepository.slettKandidatSomVeileder(aktørId, innloggetVeileder, slettetTidspunkt);

        optionalId.ifPresent(id -> {
            KandidatSlettet event = new KandidatSlettet(id, aktørId, Brukertype.VEILEDER, slettetTidspunkt);
            eventPublisher.publishEvent(event);
        });

        return optionalId;
    }

    public boolean kandidatEksisterer(String aktørId) {
        Optional<Kandidat> kandidat = kandidatRepository.hentNyesteKandidat(aktørId);
        return kandidat.isPresent();
    }
}
