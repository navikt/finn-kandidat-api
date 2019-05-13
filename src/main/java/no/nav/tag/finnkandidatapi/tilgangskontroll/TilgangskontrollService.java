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

    public boolean harSkrivetilgangTilKandidat(String fnr) {
        if (true) return true;
        return false;
    }

    public void sjekkLesetilgangTilKandidat(String fnr) {
        sjekkTilgang(fnr, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(String fnr) {
        sjekkTilgang(fnr, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(String fnr, TilgangskontrollAction action) {
        if (!veilarbabacClient.sjekkTilgang(
                hentInnloggetVeileder(),
                fnr,
                action
        )) {
            throw new TilgangskontrollException("Veileder har ikke følgende tilgang for kandidat: " + action);
        }
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }
}