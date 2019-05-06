package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.Attribute;

import java.util.List;

@Data
public class Advice {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("AttributeAssignment")
    private List<Attribute> attributeAssignment;

}
