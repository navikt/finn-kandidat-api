package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

public class FaultyArbeidssokerRegistrert extends ArbeidssokerRegistrertEvent {

    private final FailedDeserializationInfo failedDeserializationInfo;

    public FaultyArbeidssokerRegistrert(FailedDeserializationInfo failedDeserializationInfo) {
        this.failedDeserializationInfo = failedDeserializationInfo;
    }

    public FailedDeserializationInfo getFailedDeserializationInfo() {
        return this.failedDeserializationInfo;
    }
}
