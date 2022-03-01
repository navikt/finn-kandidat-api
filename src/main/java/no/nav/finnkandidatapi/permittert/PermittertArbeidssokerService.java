package no.nav.finnkandidatapi.permittert;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerSlettet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static no.nav.finnkandidatapi.permittert.PermittertArbeidssoker.endrePermittertArbeidssoker;
import static no.nav.finnkandidatapi.permittert.PermittertArbeidssoker.opprettPermittertArbeidssoker;

@Slf4j
@Service
public class PermittertArbeidssokerService {

    private final PermittertArbeidssokerRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public PermittertArbeidssokerService(PermittertArbeidssokerRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public Optional<PermittertArbeidssoker> hentNyestePermitterteArbeidssoker(String aktørId) {
        return repository.hentNyestePermittertArbeidssoker(aktørId);
    }

    public void behandleOppfølgingAvsluttet(OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding) {
        Optional<Integer> slettetKey = repository.slettPermittertArbeidssoker(oppfølgingAvsluttetMelding.getAktørId());
        if (slettetKey.isPresent()) {
            eventPublisher.publishEvent(new PermittertArbeidssokerSlettet(oppfølgingAvsluttetMelding.getAktørId()));
            log.info("Slettet Permittert Arbeidssoker med aktørid aktørid {} pga. avsluttet oppfølging", oppfølgingAvsluttetMelding.getAktørId());
        }
    }

    public void behandleArbeidssokerRegistrert(ArbeidssokerRegistrertDTO dto) {
        PermittertArbeidssoker permittertArbeidssoker =
                repository.hentNyestePermittertArbeidssoker(dto.getAktørId())
                        .map(arbeidssoker -> endrePermittertArbeidssoker(arbeidssoker, dto))
                        .orElse(opprettPermittertArbeidssoker(dto));

        Integer id = repository.lagrePermittertArbeidssoker(permittertArbeidssoker);

        Optional<PermittertArbeidssoker> lagretPermittertArbeidssoker = repository.hentPermittertArbeidssoker(id);
        lagretPermittertArbeidssoker.ifPresent(value ->
                eventPublisher.publishEvent(new PermittertArbeidssokerEndretEllerOpprettet(value))
        );
    }
}
