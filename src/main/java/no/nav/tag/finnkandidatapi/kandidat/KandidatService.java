package no.nav.tag.finnkandidatapi.kandidat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
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
        kandidat.setSistEndret(LocalDateTime.now());
    }

    public void behandleOppfølgingAvsluttet(OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding) {
        log.debug("Oppfølging avsluttet for aktørId {}", oppfølgingAvsluttetMelding.getAktorId());
        String fnr = aktørRegisterClient.tilFnr(oppfølgingAvsluttetMelding.getAktorId());
        Integer slettedeRader = slettKandidat(fnr);
        if (slettedeRader > 0) {
            log.info("Slettet {} rader", slettedeRader);
        }
    }

    public Integer slettKandidat(String fnr) {
        return kandidatRepository.slettKandidat(fnr);
    }
}
