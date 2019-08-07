package no.nav.tag.finnkandidatapi.aktørregister;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Identinfo {

    private String ident;
    private String identgruppe;
    private Boolean gjeldende;
}
