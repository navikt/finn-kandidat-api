package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Builder
public class Tilretteleggingsbehov {

    @Id
    private Integer id;
    private LocalDateTime opprettet;
    private String opprettetAvIdent;
    private String brukerFnr;
    private Arbeidstid arbeidstid;
    private ArrayList<Fysisk> fysisk;
}
