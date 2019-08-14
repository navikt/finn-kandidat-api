package no.nav.tag.finnkandidatapi.kandidat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SlettKandidat {
    private String aktorId;
    private String slettetAv;
    private LocalDateTime slettetTidspunkt;
}
