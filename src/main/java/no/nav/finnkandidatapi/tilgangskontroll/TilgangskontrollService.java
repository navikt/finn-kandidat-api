package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.NavIdent;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TilgangskontrollService {

    private final TokenUtils tokenUtils;
    private final Pep pep;

    public TilgangskontrollService(TokenUtils tokenUtils, Pep pep) {
        this.tokenUtils = tokenUtils;
        this.pep = pep;
    }

    public void sjekkLesetilgangTilKandidat(String aktørId) {
        sjekkTilgang(aktørId, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(String aktørId) {
        sjekkTilgang(aktørId, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(String aktørIdString, TilgangskontrollAction action) {
        val innloggetVeileder = tokenUtils.hentInnloggetVeileder();
        val navIdent = new NavIdent(innloggetVeileder.getNavIdent());
        val actionId = actionId(action);
        val aktørId = new AktorId(aktørIdString);

        boolean harTilgang = pep.harVeilederTilgangTilPerson(navIdent, actionId, aktørId);
        if (!harTilgang) {
            val msg = "Veileder " + innloggetVeileder + " har ikke tilgang " + action + " for aktørId " + aktørId + ".";
            throw new TilgangskontrollException(msg);
        }
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }

    private static ActionId actionId(TilgangskontrollAction action) {
        if (TilgangskontrollAction.update == action) return ActionId.WRITE;
        else if (TilgangskontrollAction.read == action) return ActionId.READ;
        else throw new RuntimeException("Uventet enum-verdi: " + action);
    }
}
