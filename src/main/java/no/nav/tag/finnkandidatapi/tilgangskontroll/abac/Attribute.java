package no.nav.tag.finnkandidatapi.tilgangskontroll.abac;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Attribute {

    @JsonProperty("AttributeId")
    private String attributeId;

    @JsonProperty("Value")
    private String value;

}
