package no.nav.tag.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.finnkandidatapi.kandidat.Brukertype;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KandidatSlettet {
    private Integer id;
    private String fnr;
    private Brukertype slettetAv;
    private LocalDateTime slettetTidspunkt;
}
