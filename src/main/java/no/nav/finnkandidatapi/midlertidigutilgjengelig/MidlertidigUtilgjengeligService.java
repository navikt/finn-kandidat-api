package no.nav.finnkandidatapi.midlertidigutilgjengelig;

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

    public boolean midlertidigTilgjengeligEksisterer(String aktørId) {
        return repository.hentMidlertidigUtilgjengelig(aktørId).isPresent();
    }

    public Optional<MidlertidigUtilgjengelig> opprettMidlertidigUtilgjengelig(MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto, Veileder innloggetVeileder) {
        LocalDateTime utilgjengeligFraDato = LocalDateTime.now();

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = MidlertidigUtilgjengelig.opprettMidlertidigUtilgjengelig(
                midlertidigUtilgjengeligDto,
                utilgjengeligFraDato,
                innloggetVeileder
        );

        Integer id = repository.lagreMidlertidigUtilgjengelig(midlertidigUtilgjengelig);
        return repository.hentMidlertidigUtilgjengeligMedId(id);
    }

    public Optional<MidlertidigUtilgjengelig> endreMidlertidigTilgjengelig(String aktørId, LocalDateTime tilDato, Veileder innloggetVeileder) {
        Integer antallOppdaterte = repository.endreMidlertidigUtilgjengelig(
                aktørId, tilDato, innloggetVeileder
        );

        if (antallOppdaterte == 0) {
            return Optional.empty();
        }

        return repository.hentMidlertidigUtilgjengelig(aktørId);
    }

    public Integer slettMidlertidigUtilgjengelig(String aktørId) {
        return repository.slettMidlertidigUtilgjengelig(
                aktørId
        );
    }
}
