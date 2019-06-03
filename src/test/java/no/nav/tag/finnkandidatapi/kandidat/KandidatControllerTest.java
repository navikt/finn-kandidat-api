package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KandidatControllerTest {

    private KandidatController controller;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private KandidatService service;

    @Mock
    private TilgangskontrollService tilgangskontroll;



    @Before
    public void setUp() {
        controller = new KandidatController(applicationEventPublisher, service, tilgangskontroll);
    }

    @Test
    public void lagreKandidat__skal_sjekke_skrivetilgang() {
        Kandidat kandidat = enKandidat();

        try {
            controller.lagreKandidat(kandidat);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidat.getFnr());
    }

    @Test
    public void lagreKandidat__skal_returnere_created_med_opprettet_kandidat() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();

        when(service.lagreKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.lagreKandidat(kandidat);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void lagreKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.lagreKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        controller.lagreKandidat(kandidat);

        verify(service).lagreKandidat(kandidat, veileder);
    }

    @Test(expected = NotFoundException.class)
    public void hentKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_fins() {
        String fnr = enKandidat().getFnr();
        when(service.hentNyesteKandidat(fnr)).thenReturn(Optional.empty());
        controller.hentKandidat(fnr);
    }

    @Test
    public void hentKandidat__skal_returnere_ok_med_kandidat() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        when(service.hentNyesteKandidat(kandidat.getFnr())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentKandidat(kandidat.getFnr());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidater__skal_returnere_ok_med_kandidater() {
        værInnloggetSom(enVeileder());

        Kandidat kandidat1 = enKandidat("1234567890");
        Kandidat kandidat2 = enKandidat("2345678901");

        when(service.hentKandidater()).thenReturn(List.of(kandidat1, kandidat2));
        when(tilgangskontroll.harLesetilgangTilKandidat(anyString())).thenReturn(true);

        ResponseEntity<List<Kandidat>> respons = controller.hentKandidater();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody().get(0)).isEqualToIgnoringGivenFields(kandidat1, "id");
        assertThat(respons.getBody().get(1)).isEqualToIgnoringGivenFields(kandidat2, "id");
    }

    @Test
    public void hentKandidater__skal_bare_returnere_kandidater_man_har_lesetilgang_til() {
        værInnloggetSom(enVeileder());

        Kandidat kandidatManHarTilgangTil = enKandidat("11111111111");
        Kandidat kandidatManIkkeHarTilgangTil = enKandidat("22222222222");

        when(service.hentKandidater()).thenReturn(List.of(kandidatManHarTilgangTil, kandidatManIkkeHarTilgangTil));
        when(tilgangskontroll.harLesetilgangTilKandidat("11111111111")).thenReturn(true);
        when(tilgangskontroll.harLesetilgangTilKandidat("22222222222")).thenReturn(false);

        ResponseEntity<List<Kandidat>> respons = controller.hentKandidater();

        assertThat(respons.getBody().get(0)).isEqualToIgnoringGivenFields(kandidatManHarTilgangTil, "id");
        assertThat(respons.getBody().size()).isEqualTo(1);
    }

    @Test(expected = FinnKandidatException.class)
    public void lagreKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_fins() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.lagreKandidat(kandidat, veileder)).thenReturn(Optional.empty());
        controller.lagreKandidat(kandidat);
    }

    @Test
    public void hentKandidat__skal_sjekke_lesetilgang() {
        String fnr = "12345678910";

        try {
            controller.hentKandidat(fnr);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkLesetilgangTilKandidat(fnr);
    }

    @Test
    public void hentSkrivetilgang__skal_returnere_ok_hvis_veileder_har_skrivetilgang() {
        værInnloggetSom(enVeileder());

        controller.hentSkrivetilgang(anyString());
        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(anyString());
    }

    @Test
    public void slettKandidat__skal_sjekke_skrivetilgang() {
        String fnr = "12345678901";

        try {
            controller.slettKandidat(fnr);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(fnr);
    }

    @Test
    public void slettKandidat__skal_returnere_ok() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        when(service.slettKandidat(kandidat.getFnr())).thenReturn(1);
        ResponseEntity<String> respons = controller.slettKandidat(kandidat.getFnr());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = NotFoundException.class)
    public void slettKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_finnes() {
        værInnloggetSom(enVeileder());
        String uregistrertFnr = "12345678901";

        when(service.slettKandidat(uregistrertFnr)).thenReturn(0);
        controller.slettKandidat(uregistrertFnr);
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tilgangskontroll.hentInnloggetVeileder()).thenReturn(veileder);
    }
}
