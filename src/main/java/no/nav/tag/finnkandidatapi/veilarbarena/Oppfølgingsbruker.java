package no.nav.tag.finnkandidatapi.veilarbarena;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Oppfølgingsbruker {

    @JsonProperty("fodselsnr")
    public String fnr;

    @JsonProperty("nav_kontor")
    public String navKontor;
}
