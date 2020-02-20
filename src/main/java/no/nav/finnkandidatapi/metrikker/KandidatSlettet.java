package no.nav.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.finnkandidatapi.kandidat.Brukertype;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KandidatSlettet {
    private Integer id;
    private String aktørId;
    private Brukertype slettetAv;
    private LocalDateTime slettetTidspunkt;
}
