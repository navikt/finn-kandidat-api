package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.AbacAction;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.AbacClient;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response.XacmlResponse;
import no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

import static no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response.Decision.Permit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TilgangskontrollService {
    private final AbacClient abacClient;
    private final TokenUtils tokenUtils;

    public boolean harSkrivetilgangTilKandidat(String fnr) {
        if (true) return true;
        return veilarbabacClient.harSkrivetilgangTilKandidat(fnr);
    }

    public void sjekkLesetilgangTilKandidat(String fnr) {
        // TODO Implementeres i TAG-500
    }

    public void sjekkSkrivetilgangTilKandidat(String fnr) {
        XacmlResponse response = abacClient.sjekkTilgang(hentInnloggetVeileder(), fnr, AbacAction.UPDATE);

        if (!Permit.equals(response.getResponse().getDecision())) {
            log.error(
                    "Veileder {} har ikke skrivetilgang til kandidat {}",
                    tokenUtils.hentInnloggetVeileder().getNavIdent(),
                    fnr
            );
            throw new TilgangskontrollException();
        }
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }
}