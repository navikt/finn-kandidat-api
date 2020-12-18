package no.nav.finnkandidatapi.samtykke;

import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.apache.commons.lang3.StringUtils;

public class SamtykkeMeldingValidator {

    public static void valider(SamtykkeMelding samtykkeMelding) {
        if (StringUtils.isBlank(samtykkeMelding.getAktoerId())) {
            throw new RuntimeException("Aktør-ID mangler");
        }

        if (StringUtils.isBlank(samtykkeMelding.getMeldingType())) {
            throw new RuntimeException("Meldingtype mangler");
        }

        if (StringUtils.isBlank(samtykkeMelding.getRessurs())) {
            throw new RuntimeException("Ressurs mangler");
        }

        if (samtykkeMelding.getOpprettetDato() == null && samtykkeMelding.getSlettetDato() == null) {
            throw new RuntimeException("OpprettetDato eller Slettetdato må ha verdi");
        }
    }
}
