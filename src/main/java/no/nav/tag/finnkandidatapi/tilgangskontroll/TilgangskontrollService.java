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

    public boolean harLesetilgangTilKandidat(String aktørId) {
        return hentTilgang(aktørId, TilgangskontrollAction.read);
    }

    public void sjekkLesetilgangTilKandidat(String aktørId) {
        sjekkTilgang(aktørId, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(String aktørId) {
        sjekkTilgang(aktørId, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(String aktørId, TilgangskontrollAction action) {
        if (!hentTilgang(aktørId, action)) {
            throw new TilgangskontrollException("Veileder har ikke følgende tilgang for kandidat: " + action);
        }
    }

    private boolean hentTilgang(String aktørId, TilgangskontrollAction action) {
        return veilarbabacClient.sjekkTilgangAktørId(
                hentInnloggetVeileder(),
                aktørId,
                action
        );
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }
}
