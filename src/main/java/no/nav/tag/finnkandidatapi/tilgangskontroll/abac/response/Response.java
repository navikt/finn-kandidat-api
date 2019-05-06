package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Response {

    @JsonProperty("Decision")
    private Decision decision;

    @JsonProperty("AssociatedAdvice")
    private Advice associatedAdvice;

}
