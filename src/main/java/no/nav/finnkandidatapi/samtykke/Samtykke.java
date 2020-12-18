package no.nav.finnkandidatapi.samtykke;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Samtykke {
    private String aktorId;
    private String foedselsnummer;
    private String gjelder;
    private String endring;
    private LocalDateTime opprettetTidspunkt;
}
