package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
public class Tilretteleggingsbehov {

    @Id
    private Integer id;
    private LocalDateTime opprettet;
    private String opprettetAvIdent;
    private String brukerFnr;
    private Arbeidstid arbeidstid;
}
