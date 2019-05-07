package no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.Attribute;

import java.util.List;

@Data
public class Advice {

    @JsonProperty("Id")
    private String id;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("AttributeAssignment")
    private List<Attribute> attributeAssignment;
}
