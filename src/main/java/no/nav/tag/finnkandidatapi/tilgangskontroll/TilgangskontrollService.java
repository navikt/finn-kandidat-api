package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TilgangskontrollService {
    private final TokenUtils tokenUtils;
    private final VeilarbabacClient  veilarbabacClient;

    public TilgangskontrollService(TokenUtils tokenUtils, VeilarbabacClient veilarbabacClient) {
        this.tokenUtils = tokenUtils;
        this.veilarbabacClient = veilarbabacClient;
    }

    public boolean harLesetilgangTilKandidat(String aktorId) {
        return hentTilgang(aktorId, TilgangskontrollAction.read);
    }

    public void sjekkLesetilgangTilKandidat(String aktorId) {
        sjekkTilgang(aktorId, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(String aktorId) {
        sjekkTilgang(aktorId, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(String aktorId, TilgangskontrollAction action) {
        if (!hentTilgang(aktorId, action)) {
            throw new TilgangskontrollException("Veileder har ikke følgende tilgang for kandidat: " + action);
        }
    }

    private boolean hentTilgang(String aktorId, TilgangskontrollAction action) {
        return veilarbabacClient.sjekkTilgangAktorId(
                hentInnloggetVeileder(),
                aktorId,
                action
        );
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }
}
