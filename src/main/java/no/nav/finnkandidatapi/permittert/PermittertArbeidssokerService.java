package no.nav.finnkandidatapi.permittert;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            log.info("Slettet Permittert Arbeidssoker med id {} pga. avsluttet oppfølging", slettetKey.get());
        }
    }

    public void behandleArbeidssokerRegistrert(ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO) {
        log.info("Har mottatt en melding fra veilarbregistrering om bruker {} som {} registrerte status {} ",
                "(anonymisert)",
                arbeidssokerRegistrertDTO.getRegistreringTidspunkt(),
                arbeidssokerRegistrertDTO.getStatus());

        PermittertArbeidssoker permittertArbeidssoker =
                repository.hentNyestePermittertArbeidssoker(arbeidssokerRegistrertDTO.getAktørId())
                        .map(arbeidssoker -> PermittertArbeidssoker.endrePermittertArbeidssoker(arbeidssoker, arbeidssokerRegistrertDTO))
                        .orElse(PermittertArbeidssoker.opprettPermittertArbeidssoker(arbeidssokerRegistrertDTO));
        Integer id = repository.lagrePermittertArbeidssoker(permittertArbeidssoker);
        Optional<PermittertArbeidssoker> lagretPermittertArbeidssoker = repository.hentPermittertArbeidssoker(id);
        lagretPermittertArbeidssoker.ifPresent(value ->
            eventPublisher.publishEvent(new PermittertArbeidssokerEndretEllerOpprettet(value))
        );
    }
}
