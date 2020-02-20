package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TilgangskontrollService {

    public static final String FINN_KANDIDAT_PILOTTILGANG_KONTOR = "finnkandidat.pilottilgang.kontor";

    private final TokenUtils tokenUtils;
    private final VeilarbabacClient veilarbabacClient;
    private final FeatureToggleService featureToggleService;

    public TilgangskontrollService(
            TokenUtils tokenUtils,
            VeilarbabacClient veilarbabacClient,
            FeatureToggleService featureToggleService
    ) {
        this.tokenUtils = tokenUtils;
        this.veilarbabacClient = veilarbabacClient;
        this.featureToggleService = featureToggleService;
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
        return veilarbabacClient.sjekkTilgang(
                hentInnloggetVeileder(),
                aktørId,
                action
        );
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }

    public void sjekkPilotTilgang() {
        if (!featureToggleService.isEnabled(FINN_KANDIDAT_PILOTTILGANG_KONTOR)) {
            throw new IkkeIPilotException("Innlogget bruker tilhører ikke et kontor som er i pilot");
        }
    }
}
