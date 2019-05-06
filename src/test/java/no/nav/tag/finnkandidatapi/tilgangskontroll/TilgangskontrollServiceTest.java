package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollServiceTest {

    private TilgangskontrollService tilgangskontroll;

    @Mock
    private VeilarbabacClient veilarbabac;

    @Mock
    private TokenUtils tokenUtils;

    @Before
    public void setUp() {
        when(tokenUtils.hentInnloggetVeileder()).thenReturn(new Veileder("Z123456"));
        tilgangskontroll = new TilgangskontrollService(veilarbabac, tokenUtils);
    }

    @Ignore
    @Test(expected = TilgangskontrollException.class)
    public void sjekkSkrivetilgangTilKandidat__skal_kaste_exception_hvis_ingen_tilgang() {
        String fnr = "123152";
        when(veilarbabac.harSkrivetilgangTilKandidat(fnr)).thenReturn(false);
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(fnr);
    }

    @Ignore
    @Test
    public void sjekkSkrivetilgangTilKandidat__skal_gå_gjennom_hvis_tilgang() {
        String fnr = "76457959";
        when(veilarbabac.harSkrivetilgangTilKandidat(fnr)).thenReturn(true);
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(fnr);
    }
}