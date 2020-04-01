package no.nav.finnkandidatapi.kandidat;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import no.nav.finnkandidatapi.permittert.ArbeidssokerRegistrertDTO;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import no.nav.finnkandidatapi.veilarbarena.Oppfølgingsbruker;
import no.nav.finnkandidatapi.veilarbarena.VeilarbArenaClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.finnkandidatapi.unleash.UnleashConfiguration.HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT;

@Slf4j
@Service
public class KandidatService {

    private static final String ENDRET_OPPFØLGING_OPPDATERTE_NAVKONTOR = "finnkandidat.endretoppfolging.oppdaterte.navkontor";

    private final KandidatRepository kandidatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AktørRegisterClient aktørRegisterClient;
    private final DateProvider dateProvider;
    private final VeilarbArenaClient veilarbarenaClient;
    private final FeatureToggleService featureToggleService;
    private final MeterRegistry meterRegistry;
    private final PermittertArbeidssokerService permittertArbeidssokerService;

    public KandidatService(
            KandidatRepository kandidatRepository,
            ApplicationEventPublisher eventPublisher,
            AktørRegisterClient aktørRegisterClient,
            DateProvider dateProvider,
            VeilarbArenaClient veilarbarenaClient,
            FeatureToggleService featureToggleService,
            MeterRegistry meterRegistry,
            PermittertArbeidssokerService permittertArbeidssokerService
    ) {
        this.kandidatRepository = kandidatRepository;
        this.eventPublisher = eventPublisher;
        this.aktørRegisterClient = aktørRegisterClient;
        this.dateProvider = dateProvider;
        this.veilarbarenaClient = veilarbarenaClient;
        this.featureToggleService = featureToggleService;
        this.meterRegistry = meterRegistry;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        meterRegistry.counter(ENDRET_OPPFØLGING_OPPDATERTE_NAVKONTOR);
    }

    public Optional<Kandidat> hentNyesteKandidat(String aktørId) {
        return kandidatRepository.hentNyesteKandidat(aktørId);
    }

    public List<Kandidat> hentKandidater() {
        return kandidatRepository.hentKandidater();
    }

    public Optional<Kandidat> opprettKandidat(KandidatDto kandidat, Veileder innloggetVeileder) {
        String navKontor = null;
        if (featureToggleService.isEnabled(HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT)) {
            Oppfølgingsbruker oppfølgingsbruker = veilarbarenaClient.hentOppfølgingsbruker(kandidat.getFnr(), kandidat.getAktørId());
            navKontor = oppfølgingsbruker.getNavKontor();
        } else {
            log.info("Setter navkontor til null siden {} er slått av", HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT);
        }

        Kandidat kandidatTilLagring = Kandidat.opprettKandidat(
                kandidat,
                innloggetVeileder,
                dateProvider.now(),
                navKontor
        );

        Integer databaseId = kandidatRepository.lagreKandidatSomVeileder(kandidatTilLagring);
        Optional<Kandidat> lagretKandidat = kandidatRepository.hentKandidat(databaseId);
        lagretKandidat.ifPresent(value -> {
            Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(value.getAktørId());
            eventPublisher.publishEvent(new KandidatOpprettet(value, permittertArbeidssoker));
        });

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
        lagretKandidat.ifPresent(value -> {
            Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(value.getAktørId());
            eventPublisher.publishEvent(new KandidatEndret(value, permittertArbeidssoker));
        });

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
        permittertArbeidssokerService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);
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

    //TODO: Gjenstår å koble sammen med Kafka-meldingene fra annen branch
    public void behandleArbeidssokerRegistrert(ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO) {
        try {
            PermittertArbeidssoker permittertArbeidssoker = permittertArbeidssokerService.behandleArbeidssokerRegistrert(arbeidssokerRegistrertDTO);
            Optional<Kandidat> optionalKandidat = hentNyesteKandidat(permittertArbeidssoker.getAktørId());
            if( optionalKandidat.isPresent() ){
                // TODO: Vil føre til at noen metrikker trigges, selv om kandidat ikke er endret.
                // Vil det være bedre å bruke PermittertArbeidssokerEndretEllerOpprettet i begge tilfeller?
                eventPublisher.publishEvent(new KandidatEndret(optionalKandidat.get(), Optional.of(permittertArbeidssoker)));
            } else {
                eventPublisher.publishEvent(new PermittertArbeidssokerEndretEllerOpprettet(permittertArbeidssoker));
            }
        } catch( Exception e) {
            log.error("Noe har feilet under behandling av ArbeidssokerRegistrertDTO", e);
        }
    }
}
