package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

import java.util.function.Function;

public class FaultyArbeidssokerRegistrertProvider implements Function<FailedDeserializationInfo, ArbeidssokerRegistrertEvent> {

    @Override
    public ArbeidssokerRegistrertEvent apply(FailedDeserializationInfo info) {
        return new FaultyArbeidssokerRegistrert(info);
    }

}


