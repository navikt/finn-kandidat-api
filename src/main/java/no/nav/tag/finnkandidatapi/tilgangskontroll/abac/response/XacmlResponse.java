package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
public class XacmlResponse {

    @JsonProperty("Response")
    private Response response;

}
