package no.nav.tag.finnkandidatapi.kandidat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.kafka.KandidatEndretProducer;
import no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.DateProvider;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KandidatService {

    private final KandidatRepository kandidatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AktørRegisterClient aktørRegisterClient;
    private final KandidatEndretProducer kandidatEndretProducer;
    private final DateProvider dateProvider;

    public Optional<Kandidat> hentNyesteKandidat(String aktørId) {
        return kandidatRepository.hentNyesteKandidat(aktørId);
    }

    public List<Kandidat> hentKandidater() {
        return kandidatRepository.hentKandidater();
    }

    public Optional<Kandidat> opprettKandidat(Kandidat kandidat, Veileder innloggetVeileder) {
        Optional<Kandidat> lagretKandidat = oppdaterSistEndretFelterOgLagreKandidat(kandidat, innloggetVeileder);

        lagretKandidat.ifPresent(value -> {
            eventPublisher.publishEvent(new KandidatOpprettet(value));
            kandidatEndretProducer.kandidatEndret(kandidat.getAktørId(), true);
        });

        return lagretKandidat;
    }

    public Optional<Kandidat> endreKandidat(Kandidat kandidat, Veileder innloggetVeileder) {
        Optional<Kandidat> lagretKandidat = oppdaterSistEndretFelterOgLagreKandidat(kandidat, innloggetVeileder);
        lagretKandidat.ifPresent(value -> eventPublisher.publishEvent(new KandidatEndret(value)));
        return lagretKandidat;
    }

    private Optional<Kandidat> oppdaterSistEndretFelterOgLagreKandidat(Kandidat kandidat, Veileder innloggetVeileder) {
        this.oppdaterSistEndretFelter(kandidat, innloggetVeileder);
        Integer id = kandidatRepository.lagreKandidat(kandidat);
        return kandidatRepository.hentKandidat(id);
    }

    private void oppdaterSistEndretFelter(Kandidat kandidat, Veileder innloggetVeileder) {
        kandidat.setSistEndretAv(innloggetVeileder.getNavIdent());
        kandidat.setSistEndret(dateProvider.now());
    }

    public void behandleOppfølgingAvsluttet(OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding) {
        Optional<Integer> slettetKey = kandidatRepository.slettKandidatSomMaskinbruker(oppfølgingAvsluttetMelding.getAktørId(), dateProvider.now());
        if (slettetKey.isPresent()) {
            kandidatEndretProducer.kandidatEndret(oppfølgingAvsluttetMelding.getAktørId(), false);
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
            kandidatEndretProducer.kandidatEndret(aktørId, false);
            eventPublisher.publishEvent(
                    new KandidatSlettet(id, aktørId, Brukertype.VEILEDER, slettetTidspunkt)
            );
        });

        return optionalId;
    }
}
