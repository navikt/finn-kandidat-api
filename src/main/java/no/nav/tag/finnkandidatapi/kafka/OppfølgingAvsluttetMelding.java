package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OppfølgingAvsluttetMelding {

    @JsonProperty("aktorId")
    private String aktørId;
    private Date sluttdato;
}
