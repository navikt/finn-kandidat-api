package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KandidatControllerTest {

    private KandidatController controller;

    @Mock
    private KandidatService service;

    @Mock
    private TilgangskontrollService tilgangskontroll;

    @Before
    public void setUp() {
        controller = new KandidatController(service, tilgangskontroll);
    }

    @Test
    public void opprettKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        try {
            controller.opprettKandidat(kandidat);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());
    }

    @Test
    public void opprettKandidat__skal_returnere_created_med_opprettet_kandidat() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();

        when(service.opprettKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidat);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void opprettKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.opprettKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        controller.opprettKandidat(kandidat);

        verify(service).opprettKandidat(kandidat, veileder);
    }

    @Test(expected = FinnKandidatException.class)
    public void opprettKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_fins() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.opprettKandidat(kandidat, veileder)).thenReturn(Optional.empty());
        controller.opprettKandidat(kandidat);
    }

    @Test
    public void endreKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        try {
            controller.endreKandidat(kandidat);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());
    }

    @Test
    public void endreKandidat__skal_returnere_ok_med_opprettet_kandidat() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();

        when(service.endreKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.endreKandidat(kandidat);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void endreKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.endreKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        controller.endreKandidat(kandidat);

        verify(service).endreKandidat(kandidat, veileder);
    }

    @Test(expected = FinnKandidatException.class)
    public void endreKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_fins() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.endreKandidat(kandidat, veileder)).thenReturn(Optional.empty());
        controller.endreKandidat(kandidat);
    }

    @Test
    public void hentKandidater__skal_returnere_ok_med_kandidater() {
        værInnloggetSom(enVeileder());

        Kandidat kandidat1 = enKandidat("1000000000001");
        Kandidat kandidat2 = enKandidat("1000000000002");

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

        Kandidat kandidatManHarTilgangTil = enKandidat("1000000000001");
        Kandidat kandidatManIkkeHarTilgangTil = enKandidat("1000000000002");

        when(service.hentKandidater()).thenReturn(List.of(kandidatManHarTilgangTil, kandidatManIkkeHarTilgangTil));
        when(tilgangskontroll.harLesetilgangTilKandidat("1000000000001")).thenReturn(true);
        when(tilgangskontroll.harLesetilgangTilKandidat("1000000000002")).thenReturn(false);

        ResponseEntity<List<Kandidat>> respons = controller.hentKandidater();

        assertThat(respons.getBody().get(0)).isEqualToIgnoringGivenFields(kandidatManHarTilgangTil, "id");
        assertThat(respons.getBody().size()).isEqualTo(1);
    }

    @Test(expected = NotFoundException.class)
    public void hentKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_fins() {
        værInnloggetSom(enVeileder());
        String aktørId = enKandidat().getAktørId();
        when(service.hentNyesteKandidat(aktørId)).thenReturn(Optional.empty());
        controller.hentKandidat(aktørId);
    }

    @Test
    public void hentKandidat__skal_returnere_ok_med_kandidat() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentKandidat(kandidat.getAktørId());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidat__skal_sjekke_lesetilgang() {
        værInnloggetSom(enVeileder());
        String aktørId = "1000000000001";

        try {
            controller.hentKandidat(aktørId);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkLesetilgangTilKandidat(aktørId);
    }

    @Test
    public void hentAktørId__skal_returnere_ok_med_atkørId() {
        værInnloggetSom(enVeileder());
        String fnr = "02020963312";
        String aktørId = "1000000000001";
        when(service.hentAktørId(fnr)).thenReturn(aktørId);

        ResponseEntity<String> respons = controller.hentAktørId(fnr);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(aktørId);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentAktørId__skal_kaste_FinnKandidatException_hvis_kall_til_aktørregister_feiler() {
        værInnloggetSom(enVeileder());
        String fnr = "02020963312";
        when(service.hentAktørId(fnr)).thenThrow(FinnKandidatException.class);

        controller.hentAktørId(fnr);
    }

    @Test
    public void hentSkrivetilgang__skal_returnere_ok_hvis_veileder_har_skrivetilgang() {
        værInnloggetSom(enVeileder());
        String aktørId = "1000000000001";

        controller.hentSkrivetilgang(aktørId);
        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(aktørId);
    }

    @Test
    public void slettKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(enVeileder());
        String aktørId = "1000000000001";

        try {
            controller.slettKandidat(aktørId );
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(aktørId);
    }

    @Test
    public void slettKandidat__skal_returnere_ok() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();

        when(service.slettKandidat(kandidat.getAktørId(), veileder)).thenReturn(Optional.of(1));
        ResponseEntity<String> respons = controller.slettKandidat(kandidat.getAktørId());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = NotFoundException.class)
    public void slettKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_finnes() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        String uregistrertAktørId = "1000000000001";

        controller.slettKandidat(uregistrertAktørId);
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tilgangskontroll.hentInnloggetVeileder()).thenReturn(veileder);
    }
}
