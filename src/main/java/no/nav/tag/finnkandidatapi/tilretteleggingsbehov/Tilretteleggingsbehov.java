package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Tilretteleggingsbehov {
    private Integer id;
    private LocalDateTime opprettet;
    private String opprettetAvIdent;
    private String brukerFnr;
    private Arbeidstid arbeidstid;
    private List<Fysisk> fysisk;
    private List<Arbeidsmiljo> arbeidsmiljo;
    private List<Grunnleggende> grunnleggende;
}
