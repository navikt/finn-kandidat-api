package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TilgangskontrollService {
    private final TokenUtils tokenUtils;

    public boolean harSkrivetilgangTilKandidat(String fnr) {
        // TODO Implementeres i TAG-363
        return true;
    }

    public void sjekkSkrivetilgangTilKandidat(String fnr) {
        // TODO Implementeres i TAG-363
        return;
    }

    public void sjekkLesetilgangTilKandidat(String fnr) {
        // TODO Implementeres i TAG-500
    }

    public Veileder hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetVeileder();
    }
}