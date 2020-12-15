package no.nav.finnkandidatapi.samtykke;

import lombok.*;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Samtykke {
    private String aktoerId;
    private String gjelder;
    private String endring;
    private ZonedDateTime opprettetTidspunkt;
}
