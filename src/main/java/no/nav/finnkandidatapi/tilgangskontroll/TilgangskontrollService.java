package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import org.springframework.stereotype.Service;

import static no.nav.finnkandidatapi.unleash.UnleashConfiguration.ABAC_UTEN_VEILARBABAC;

@Slf4j
@Service
public class TilgangskontrollService {

    public static final String FINN_KANDIDAT_PILOTTILGANG_KONTOR = "finnkandidat.pilottilgang.kontor";

    private final TokenUtils tokenUtils;
    private final VeilarbabacClient veilarbabacClient;
    private final FeatureToggleService featureToggle;

    public TilgangskontrollService(
            TokenUtils tokenUtils,
            VeilarbabacClient veilarbabacClient,
            FeatureToggleService featureToggle
    ) {
        this.tokenUtils = tokenUtils;
        this.veilarbabacClient = veilarbabacClient;
        this.featureToggle = featureToggle;
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
        if (featureToggle.isEnabled(ABAC_UTEN_VEILARBABAC)) {
            log.info("Feature toggle enabled: [" + ABAC_UTEN_VEILARBABAC + "]");
            return false; // TODO Are
        } else {
            log.info("Feature toggle disabled: [" + ABAC_UTEN_VEILARBABAC + "]");
            return veilarbabacClient.sjekkTilgang(
                    hentInnloggetVeileder(),
                    aktørId,
                    action
            );
        }
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }
}
