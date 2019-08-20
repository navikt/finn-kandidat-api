package no.nav.tag.finnkandidatapi.kandidat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SlettKandidat {
    private String aktørId;
    private String slettetAv;
    private LocalDateTime slettetTidspunkt;
}
