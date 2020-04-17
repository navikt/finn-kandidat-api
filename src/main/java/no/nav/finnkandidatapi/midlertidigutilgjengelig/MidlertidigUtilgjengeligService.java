package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.stereotype.Service;

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
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = MidlertidigUtilgjengelig.opprettMidlertidigUtilgjengelig(
                midlertidigUtilgjengeligDto,
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

    public Optional<MidlertidigUtilgjengelig> forlengeMidlertidigUtilgjengelig(MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto, Veileder innloggetVeileder) {
        Integer antallOppdaterte = repository.forlengeMidlertidigUtilgjengelig(
                midlertidigUtilgjengeligDto.getAktørId(), midlertidigUtilgjengeligDto.getTilDato(), innloggetVeileder
        );

        if (antallOppdaterte != 0) {
            throw new FinnKandidatException();
        }

        return repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengeligDto.getAktørId());
    }
}
