package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class XacmlResponse {

    @JsonProperty("Response")
    private Response response;

}
