package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.NotFoundException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MidlertidigUtilgjengeligService {
    private final MidlertidigUtilgjengeligRepository repository;

    public MidlertidigUtilgjengeligService(MidlertidigUtilgjengeligRepository repository) {
        this.repository = repository;
    }

    public Optional<MidlertidigUtilgjengelig> hentMidlertidigUtilgjengelig(String aktørId) {
        return repository.hentMidlertidigUtilgjengelig(aktørId);
    }

    public MidlertidigUtilgjengelig opprettMidlertidigUtilgjengelig(MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto, Veileder innloggetVeileder) {
        LocalDateTime utilgjengeligFraIdag = LocalDateTime.now();

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = MidlertidigUtilgjengelig.opprettMidlertidigUtilgjengelig(
                midlertidigUtilgjengeligDto,
                utilgjengeligFraIdag,
                innloggetVeileder
        );

        Optional<MidlertidigUtilgjengelig> eksisterendeMidlertidigUtilgjengelig = repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengeligDto.getAktørId());
        eksisterendeMidlertidigUtilgjengelig.ifPresent(error -> {
            throw new AlleredeRegistrertException("Det er allerede registrert at kandidaten er midlertidig utilgjengelig");
        });

        Integer id = repository.lagreMidlertidigUtilgjengelig(midlertidigUtilgjengelig);
        Optional<MidlertidigUtilgjengelig> lagretUtilgjengelighet = repository.hentMidlertidigUtilgjengeligMedId(id);

        if (lagretUtilgjengelighet.isEmpty()) {
            throw new FinnKandidatException();
        }

        return lagretUtilgjengelighet.get();
    }

    public Optional<MidlertidigUtilgjengelig> forlengeMidlertidigUtilgjengelig(String aktørId, LocalDateTime tilDato, Veileder innloggetVeileder) {
        Integer antallOppdaterte = repository.forlengeMidlertidigUtilgjengelig(
                aktørId, tilDato, innloggetVeileder
        );

        if (antallOppdaterte == 0) {
            throw new NotFoundException();
        }

        return repository.hentMidlertidigUtilgjengelig(aktørId);
    }

    public void slettMidlertidigUtilgjengelig(String aktørId) {
        Integer antallOppdaterteRader = repository.slettMidlertidigUtilgjengelig(
                aktørId
        );

        if (antallOppdaterteRader == 0) {
            throw new NotFoundException();
        }
    }
}
