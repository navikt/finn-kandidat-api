package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligEndret;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligOpprettet;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligSlettet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MidlertidigUtilgjengeligService {
    private final MidlertidigUtilgjengeligRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MidlertidigUtilgjengeligService(MidlertidigUtilgjengeligRepository repository, ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
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
        var response = repository.hentMidlertidigUtilgjengeligMedId(id);

        applicationEventPublisher.publishEvent(new MidlertidigUtilgjengeligOpprettet(response.get()));

        return response;
    }

    public Optional<MidlertidigUtilgjengelig> endreMidlertidigTilgjengelig(String aktørId, LocalDateTime tilDato, Veileder innloggetVeileder) {
        Integer antallOppdaterte = repository.endreMidlertidigUtilgjengelig(
                aktørId, tilDato, innloggetVeileder
        );

        if (antallOppdaterte == 0) {
            return Optional.empty();
        }

        var response = repository.hentMidlertidigUtilgjengelig(aktørId);

        applicationEventPublisher.publishEvent(new MidlertidigUtilgjengeligEndret(
                MidlertidigUtilgjengelig.builder()
                        .aktørId(aktørId)
                        .tilDato(tilDato)
                        .sistEndretAvIdent(innloggetVeileder.getNavIdent())
                        .sistEndretAvNavn(innloggetVeileder.getNavn())
                        .build()));

        return response;
    }

    public Integer slettMidlertidigUtilgjengelig(String aktørId, Veileder innloggetVeileder) {
        var response = repository.slettMidlertidigUtilgjengelig(
                aktørId
        );
        applicationEventPublisher.publishEvent(
                new MidlertidigUtilgjengeligSlettet(MidlertidigUtilgjengelig.builder()
                        .aktørId(aktørId)
                        .sistEndretAvIdent(innloggetVeileder.getNavIdent())
                        .sistEndretAvNavn(innloggetVeileder.getNavn())
                        .build())
        );

        return response;
    }
}
