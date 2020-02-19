package no.nav.finnkandidatapi.kandidat;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

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
        Veileder veileder = TestData.enVeileder();
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto(kandidat);

        værInnloggetSom(veileder);
        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        controller.opprettKandidat(kandidatDto);

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());
    }

    @Test
    public void opprettKandidat__skal_returnere_created_med_opprettet_kandidat() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto(kandidat);

        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidatDto);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void opprettKandidat__skal_fungere_uten_aktørId() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto(kandidat);

        kandidatDto.setAktørId(null);

        when(service.hentAktørId(kandidatDto.getFnr())).thenReturn(kandidat.getAktørId());
        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidatDto);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void opprettKandidat__skal_fungere_uten_fnr() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto(kandidat);

        kandidatDto.setFnr(null);

        when(service.hentFnr(kandidatDto.getAktørId())).thenReturn(kandidat.getFnr());
        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidatDto);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void opprettKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto(kandidat);
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);

        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        controller.opprettKandidat(kandidatDto);

        verify(service).opprettKandidat(kandidatDto, veileder);
    }

    @Test(expected = FinnKandidatException.class)
    public void opprettKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_ble_lagret() {
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto(kandidat);
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);

        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.empty());

        controller.opprettKandidat(kandidatDto);
    }

    @Test
    public void opprettKandidat__skal_returnere_conflict_hvis_kandidat_eksisterer() {
        KandidatDto kandidatDto = TestData.enKandidatDto();
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);

        when(service.kandidatEksisterer(kandidatDto.getAktørId())).thenReturn(true);

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidatDto);
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void endreKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(TestData.enVeileder());
        KandidatDto kandidatDto = TestData.enKandidatDto();

        try {
            controller.endreKandidat(kandidatDto);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidatDto.getAktørId());
    }

    @Test
    public void endreKandidat__skal_returnere_ok_med_opprettet_kandidat() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto();

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.endreKandidat(kandidatDto);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void endreKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = TestData.enKandidatDto();
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        controller.endreKandidat(kandidatDto);

        verify(service).endreKandidat(kandidatDto, veileder);
    }

    @Test(expected = FinnKandidatException.class)
    public void endreKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_fins() {
        KandidatDto kandidatDto = TestData.enKandidatDto();
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.empty());

        controller.endreKandidat(kandidatDto);
    }

    @Test
    public void endreKandidat__skal_fungere_med_kun_fnr() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = TestData.enKandidat();
        KandidatDto kandidatDto = KandidatDto.builder()
                .fnr(TestData.etFnr())
                .build();

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.endreKandidat(kandidatDto);

        verify(service).endreKandidat(kandidatDto, veileder);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidater__skal_returnere_ok_med_kandidater() {
        værInnloggetSom(TestData.enVeileder());

        Kandidat kandidat1 = TestData.enKandidat("1000000000001");
        Kandidat kandidat2 = TestData.enKandidat("1000000000002");

        when(service.hentKandidater()).thenReturn(List.of(kandidat1, kandidat2));
        when(tilgangskontroll.harLesetilgangTilKandidat(anyString())).thenReturn(true);

        ResponseEntity<List<Kandidat>> respons = controller.hentKandidater();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody().get(0)).isEqualToIgnoringGivenFields(kandidat1, "id");
        assertThat(respons.getBody().get(1)).isEqualToIgnoringGivenFields(kandidat2, "id");
    }

    @Test
    public void hentKandidater__skal_bare_returnere_kandidater_man_har_lesetilgang_til() {
        værInnloggetSom(TestData.enVeileder());

        Kandidat kandidatManHarTilgangTil = TestData.enKandidat("1000000000001");
        Kandidat kandidatManIkkeHarTilgangTil = TestData.enKandidat("1000000000002");

        when(service.hentKandidater()).thenReturn(List.of(kandidatManHarTilgangTil, kandidatManIkkeHarTilgangTil));
        when(tilgangskontroll.harLesetilgangTilKandidat("1000000000001")).thenReturn(true);
        when(tilgangskontroll.harLesetilgangTilKandidat("1000000000002")).thenReturn(false);

        ResponseEntity<List<Kandidat>> respons = controller.hentKandidater();

        assertThat(respons.getBody().get(0)).isEqualToIgnoringGivenFields(kandidatManHarTilgangTil, "id");
        assertThat(respons.getBody().size()).isEqualTo(1);
    }

    @Test(expected = NotFoundException.class)
    public void hentKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_fins() {
        værInnloggetSom(TestData.enVeileder());
        String aktørId = TestData.enKandidat().getAktørId();
        when(service.hentNyesteKandidat(aktørId)).thenReturn(Optional.empty());
        controller.hentKandidat(aktørId);
    }


    @Test
    public void hentKandidat__med_fnr_skal_returnere_ok_med_kandidat() {
        værInnloggetSom(TestData.enVeileder());
        Kandidat kandidat = TestData.enKandidat();

        when(service.hentAktørId(kandidat.getFnr())).thenReturn(kandidat.getAktørId());
        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentKandidat(kandidat.getFnr());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidat__skal_returnere_ok_med_kandidat() {
        værInnloggetSom(TestData.enVeileder());
        Kandidat kandidat = TestData.enKandidat();

        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentKandidat(kandidat.getAktørId());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidat__skal_sjekke_lesetilgang() {
        værInnloggetSom(TestData.enVeileder());
        String aktørId = "1000000000001";
        when(service.hentNyesteKandidat(aktørId)).thenReturn(Optional.of(TestData.enKandidat()));

        controller.hentKandidat(aktørId);

        verify(tilgangskontroll, times(1)).sjekkLesetilgangTilKandidat(aktørId);
    }

    @Test
    public void hentAktørId__skal_returnere_ok_med_aktørId() {
        værInnloggetSom(TestData.enVeileder());
        String fnr = "02020963312";
        String aktørId = "1000000000001";
        when(service.hentAktørId(fnr)).thenReturn(aktørId);

        ResponseEntity<String> respons = controller.hentAktørId(fnr);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(aktørId);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentAktørId__skal_kaste_FinnKandidatException_hvis_kall_til_aktørregister_feiler() {
        værInnloggetSom(TestData.enVeileder());
        String fnr = "02020963312";
        when(service.hentAktørId(fnr)).thenThrow(FinnKandidatException.class);

        controller.hentAktørId(fnr);
    }

    @Test
    public void hentFnr__skal_returnere_ok_med_fnr() {
        værInnloggetSom(TestData.enVeileder());
        String fnr = "02020963312";
        String aktørId = "1000000000001";
        when(service.hentFnr(aktørId)).thenReturn(fnr);

        ResponseEntity<String> respons = controller.hentFnr(aktørId);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(fnr);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentFnr__skal_kaste_FinnKandidatException_hvis_kall_til_aktørregister_feiler() {
        værInnloggetSom(TestData.enVeileder());
        String aktørId = "02020963312";
        when(service.hentFnr(aktørId)).thenThrow(FinnKandidatException.class);

        controller.hentFnr(aktørId);
    }

    @Test
    public void hentSkrivetilgang__skal_returnere_ok_hvis_veileder_har_skrivetilgang() {
        værInnloggetSom(TestData.enVeileder());
        String aktørId = "1000000000001";

        controller.hentSkrivetilgang(aktørId);
        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(aktørId);
    }

    @Test
    public void slettKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(TestData.enVeileder());
        String aktørId = "1000000000001";

        controller.slettKandidat(aktørId);
        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(aktørId);
    }

    @Test
    public void slettKandidat__skal_returnere_ok() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = TestData.enKandidat();

        when(service.slettKandidat(kandidat.getAktørId(), veileder)).thenReturn(Optional.of(1));
        ResponseEntity respons = controller.slettKandidat(kandidat.getAktørId());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void slettKandidat__skal_fungere_med_kun_fnr() {
        Veileder veileder = TestData.enVeileder();
        værInnloggetSom(veileder);
        String fnr = TestData.etFnr();
        String aktørId = TestData.enAktørId();

        when(service.hentAktørId(fnr)).thenReturn(aktørId);
        when(service.slettKandidat(aktørId, veileder)).thenReturn(Optional.of(1));

        ResponseEntity respons = controller.slettKandidat(fnr);
        verify(service).slettKandidat(aktørId, veileder);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void slettKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_finnes() {
        værInnloggetSom(TestData.enVeileder());
        String uregistrertAktørId = "1000000000001";

        ResponseEntity respons = controller.slettKandidat(uregistrertAktørId);
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tilgangskontroll.hentInnloggetVeileder()).thenReturn(veileder);
    }
}
