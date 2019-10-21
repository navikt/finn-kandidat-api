package no.nav.tag.finnkandidatapi.kandidat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.Unleash;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.DateProvider;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.tag.finnkandidatapi.unleash.UnleashConfiguration;
import no.nav.tag.finnkandidatapi.veilarbarena.Personinfo;
import no.nav.tag.finnkandidatapi.veilarbarena.VeilarbArenaClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.unleash.UnleashConfiguration.HENT_PERSONINFO_OPPRETT_KANDIDAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class KandidatService {

    private final KandidatRepository kandidatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AktørRegisterClient aktørRegisterClient;
    private final DateProvider dateProvider;
    private final VeilarbArenaClient veilarbarenaClient;
    private final Unleash unleash;

    public Optional<Kandidat> hentNyesteKandidat(String aktørId) {
        return kandidatRepository.hentNyesteKandidat(aktørId);
    }

    public List<Kandidat> hentKandidater() {
        return kandidatRepository.hentKandidater();
    }

    public Optional<Kandidat> opprettKandidat(String fnr, KandidatDto kandidat, Veileder innloggetVeileder) {
        String navKontor = null;
        if (unleash.isEnabled(HENT_PERSONINFO_OPPRETT_KANDIDAT)) {
            Personinfo personinfo = veilarbarenaClient.hentPersoninfo(fnr);
            navKontor = personinfo.getNavKontor();
        } else {
            log.info("Setter navkontor til null siden {} er slått av", HENT_PERSONINFO_OPPRETT_KANDIDAT);
        }

        Kandidat kandidatTilLagring = Kandidat.opprettKandidat(
                fnr,
                kandidat,
                innloggetVeileder,
                dateProvider.now(),
                navKontor
        );

        Integer databaseId = kandidatRepository.lagreKandidat(kandidatTilLagring);
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

        Integer id = kandidatRepository.lagreKandidat(endretkandidat);
        Optional<Kandidat> lagretKandidat = kandidatRepository.hentKandidat(id);
        lagretKandidat.ifPresent(value -> eventPublisher.publishEvent(new KandidatEndret(value)));

        return lagretKandidat;
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
        Optional<Integer> optionalId = kandidatRepository.slettKandidat(aktørId, innloggetVeileder, slettetTidspunkt);

        optionalId.ifPresent(id -> {
            eventPublisher.publishEvent(
                    new KandidatSlettet(id, aktørId, Brukertype.VEILEDER, slettetTidspunkt)
            );
        });

        return optionalId;
    }

    public boolean kandidatEksisterer(String aktørId) {
        Optional<Kandidat> kandidat = kandidatRepository.hentNyesteKandidat(aktørId);
        return kandidat.isPresent();
    }
}
