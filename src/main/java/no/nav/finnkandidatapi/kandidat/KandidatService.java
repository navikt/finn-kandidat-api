package no.nav.finnkandidatapi.kandidat;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class KandidatService {

    private final KandidatRepository kandidatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AktorOppslagClient aktorOppslagClient;
    private final DateProvider dateProvider;

    public KandidatService(
            KandidatRepository kandidatRepository,
            ApplicationEventPublisher eventPublisher,
            AktorOppslagClient aktorOppslagClient,
            DateProvider dateProvider
    ) {
        this.kandidatRepository = kandidatRepository;
        this.eventPublisher = eventPublisher;
        this.aktorOppslagClient = aktorOppslagClient;
        this.dateProvider = dateProvider;
    }

    public Optional<Kandidat> hentNyesteKandidat(String aktørId) {
        return kandidatRepository.hentNyesteKandidat(aktørId);
    }

    public Optional<Kandidat> opprettKandidat(KandidatDto kandidat, Veileder innloggetVeileder) {
        Kandidat kandidatTilLagring = Kandidat.opprettKandidat(
                kandidat,
                innloggetVeileder,
                dateProvider.now()
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

    public void behandleOppfølgingAvsluttet(SisteOppfolgingsperiodeV1 sisteOppfolgingsperiode) {
        Optional<Integer> slettetKey = kandidatRepository.slettKandidatSomMaskinbruker(sisteOppfolgingsperiode.getAktorId(), dateProvider.now());
        if (slettetKey.isPresent()) {
            eventPublisher.publishEvent(new KandidatSlettet(slettetKey.get(), sisteOppfolgingsperiode.getAktorId(), Brukertype.SYSTEM, dateProvider.now()));
            log.info("Slettet kandidat med id {} pga. avsluttet oppfølging", slettetKey.get());
        }
    }

    public String hentAktørId(String fnr) {
        return aktorOppslagClient.hentAktorId(new Fnr(fnr)).get();
    }

    public String hentFnr(String aktørId) {
        return aktorOppslagClient.hentFnr(new AktorId(aktørId)).get();
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
