package no.nav.tag.finnkandidatapi.tilgangskontroll.sts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class STStoken {
    String access_token;
    String token_type;
    int expires_in;
}
