package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.NotFoundException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private void sjekkAtDatoErIFremtiden(LocalDateTime tilDato) {
        LocalDate idag = LocalDate.now();
        LocalDateTime idagMidnatt = LocalDateTime.of(idag, LocalTime.MIDNIGHT);
        if (tilDato.isBefore(idagMidnatt)) {
            throw new BadRequestException("Du kan ikke sette en kandidat som midlertidig utilgjengelig tilbake i tid");
        }
    }

    public MidlertidigUtilgjengelig opprettMidlertidigUtilgjengelig(MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto, Veileder innloggetVeileder) {
        LocalDateTime utilgjengeligFraDato = LocalDateTime.now();
        sjekkAtDatoErIFremtiden(midlertidigUtilgjengeligDto.getTilDato());

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = MidlertidigUtilgjengelig.opprettMidlertidigUtilgjengelig(
                midlertidigUtilgjengeligDto,
                utilgjengeligFraDato,
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
        sjekkAtDatoErIFremtiden(tilDato);

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
