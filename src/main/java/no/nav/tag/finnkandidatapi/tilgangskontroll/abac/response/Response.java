package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
public class Response {

    @JsonProperty("Decision")
    private Decision decision;

    @JsonProperty("AssociatedAdvice")
    private Advice associatedAdvice;

}
