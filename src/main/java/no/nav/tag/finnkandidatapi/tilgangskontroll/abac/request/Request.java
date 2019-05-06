package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.Attributes;

@Builder
@Data
public class Request {

    @JsonProperty("Environment")
    private Attributes environment;
    @JsonProperty("Action")
    private Attributes action;
    @JsonProperty("Resource")
    private Attributes resource;

    @JsonProperty("AccessSubject")
    private Attributes accessSubject;

}
