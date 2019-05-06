package no.nav.tag.finnkandidatapi.tilgangskontroll.abac;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Attributes {

    @JsonProperty("Attribute")
    private List<Attribute> attributes = new ArrayList<>();

    public Attributes addAttribute(String name, String value) {
        attributes.add(Attribute.builder().attributeId(name).value(value).build());
        return this;
    }

}
