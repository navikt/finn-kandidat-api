package no.nav.tag.finnkandidatapi.unleash.enhet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AxsysEnhet {
    private String enhetId;
}
