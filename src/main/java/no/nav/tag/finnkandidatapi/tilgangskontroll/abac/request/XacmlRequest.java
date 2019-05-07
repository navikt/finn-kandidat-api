package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class XacmlRequest {

    @JsonProperty("Request")
    private Request request;

    public XacmlRequest withRequest(Request request) {
        this.request = request;
        return this;
    }

}
