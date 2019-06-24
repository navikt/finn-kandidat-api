package no.nav.tag.finnkandidatapi.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OppfølgingAvsluttetMelding {

    private String aktorId;
    private Date sluttdato;
}
