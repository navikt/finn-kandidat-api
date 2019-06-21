package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.DateProvider;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KandidatService {

    private final KandidatRepository kandidatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DateProvider dateProvider;

    public KandidatService(KandidatRepository kandidatRepository, ApplicationEventPublisher eventPublisher, DateProvider dateProvider) {
        this.kandidatRepository = kandidatRepository;
        this.eventPublisher = eventPublisher;
        this.dateProvider = dateProvider;
    }

    public Optional<Kandidat> hentNyesteKandidat(String fnr) {
        return kandidatRepository.hentNyesteKandidat(fnr);
    }

    public List<Kandidat> hentKandidater() {
        return kandidatRepository.hentKandidater();
    }

    public Optional<Kandidat> opprettKandidat(Kandidat kandidat, Veileder innloggetVeileder) {
        Optional<Kandidat> lagretKandidat = oppdaterSistEndretFelterOgLagreKandidat(kandidat, innloggetVeileder);
        lagretKandidat.ifPresent(value -> eventPublisher.publishEvent(new KandidatOpprettet(value)));
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

    Optional<Integer> slettKandidat(String fnr, Veileder innloggetVeileder) {
        SlettKandidat slettKandidat = new SlettKandidat(
                fnr,
                innloggetVeileder.getNavIdent(),
                dateProvider.now()
        );
        Optional<Integer> id = kandidatRepository.slettKandidat(slettKandidat);
        eventPublisher.publishEvent(slettKandidat);
        return id;
    }
}
