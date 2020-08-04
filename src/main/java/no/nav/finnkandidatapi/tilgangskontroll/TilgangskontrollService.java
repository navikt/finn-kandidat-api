package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.abac.exception.PepException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import org.springframework.stereotype.Service;

import static no.nav.common.abac.domain.AbacPersonId.aktorId;
import static no.nav.finnkandidatapi.unleash.UnleashConfiguration.ABAC_UTEN_VEILARBABAC;

@Slf4j
@Service
public class TilgangskontrollService {

    private final TokenUtils tokenUtils;
    private final VeilarbabacClient veilarbabacClient;
    private final FeatureToggleService featureToggle;
    private final Pep pep;

    public TilgangskontrollService(
            TokenUtils tokenUtils,
            VeilarbabacClient veilarbabacClient,
            FeatureToggleService featureToggle,
            Pep pep
    ) {
        this.tokenUtils = tokenUtils;
        this.veilarbabacClient = veilarbabacClient;
        this.featureToggle = featureToggle;
        this.pep = pep;
    }

    public void sjekkLesetilgangTilKandidat(String aktørId) {
        sjekkTilgang(aktørId, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(String aktørId) {
        sjekkTilgang(aktørId, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(String aktørId, TilgangskontrollAction action) {
        val innloggetVeileder = hentInnloggetVeileder();
        if (!hentTilgang(innloggetVeileder, aktørId, action)) {
            val msg = "Veileder " + innloggetVeileder + " har ikke tilgang " + action + " for aktørId " + aktørId + ".";
            throw new TilgangskontrollException(msg);
        }
    }

    private boolean hentTilgang(Veileder veileder, String aktørId, TilgangskontrollAction action) {
        if (featureToggle.isEnabled(ABAC_UTEN_VEILARBABAC)) {
            log.info("Feature toggle enabled: [" + ABAC_UTEN_VEILARBABAC + "]");
            val actionId = actionId(action);
            val personId = aktorId(aktørId);
            try {
                pep.sjekkVeilederTilgangTilBruker(veileder.getNavIdent(), actionId, personId);
            } catch (PepException e) {
                val msg = "Veileder " + veileder + " har ikke tilgang " + action + " for aktørId " + aktørId + ".";
                log.debug(msg, e);
                return false;
            } catch (Exception e) {
                val msg = "Forsøkte å sjekke tilgang i ABAC";
                log.error(msg, e);
                return false;
            }
            return true;
        } else {
            log.info("Feature toggle disabled: [" + ABAC_UTEN_VEILARBABAC + "]");
            return veilarbabacClient.sjekkTilgang(
                    veileder,
                    aktørId,
                    action
            );
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
