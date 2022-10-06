package no.nav.finnkandidatapi.kandidat;

import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.*;
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

    @AfterEach
    public void afterEach() {
        resetClusternavnIKandidatController();
        controller.konfigurerFødselsnummerValidator();
    }

    @Test
    public void opprettKandidat__skal_sjekke_skrivetilgang() {
        Veileder veileder = enVeileder();
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);

        værInnloggetSom(veileder);
        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        controller.opprettKandidat(kandidatDto);

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());
    }

    @Test
    public void opprettKandidat__skal_returnere_created_med_opprettet_kandidat() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);

        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidatDto);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void opprettKandidat__skal_fungere_uten_aktørId() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);

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
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);

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
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        controller.opprettKandidat(kandidatDto);

        verify(service).opprettKandidat(kandidatDto, veileder);
    }

    @Test(expected = FinnKandidatException.class)
    public void opprettKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_ble_lagret() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.opprettKandidat(kandidatDto, veileder)).thenReturn(Optional.empty());

        controller.opprettKandidat(kandidatDto);
    }

    @Test
    public void opprettKandidat__skal_returnere_conflict_hvis_kandidat_eksisterer() {
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.kandidatEksisterer(kandidatDto.getAktørId())).thenReturn(true);

        ResponseEntity<Kandidat> respons = controller.opprettKandidat(kandidatDto);
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void endreKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(enVeileder());
        KandidatDto kandidatDto = enKandidatDto();

        try {
            controller.endreKandidat(kandidatDto);
        } catch (Exception ignored) {}

        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(kandidatDto.getAktørId());
    }

    @Test
    public void endreKandidat__skal_returnere_ok_med_opprettet_kandidat() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.endreKandidat(kandidatDto);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void endreKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        controller.endreKandidat(kandidatDto);

        verify(service).endreKandidat(kandidatDto, veileder);
    }

    @Test(expected = FinnKandidatException.class)
    public void endreKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_fins() {
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.empty());

        controller.endreKandidat(kandidatDto);
    }

    @Test
    public void endreKandidat__skal_fungere_med_kun_fnr() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = KandidatDto.builder()
                .fnr(etFnr())
                .build();

        when(service.endreKandidat(kandidatDto, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.endreKandidat(kandidatDto);

        verify(service).endreKandidat(kandidatDto, veileder);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test(expected = NotFoundException.class)
    public void hentKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_fins() {
        værInnloggetSom(enVeileder());
        String aktørId = enKandidat().getAktørId();
        when(service.hentNyesteKandidat(aktørId)).thenReturn(Optional.empty());
        controller.hentKandidat(aktørId);
    }


    @Test
    public void hentKandidat__med_fnr_skal_returnere_ok_med_kandidat() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        when(service.hentAktørId(kandidat.getFnr())).thenReturn(kandidat.getAktørId());
        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentKandidat(kandidat.getFnr());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidat__med_syntetisk_fnr_skal_returnere_ok_med_kandidat_i_dev() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidatMedSyntetiskFødselsnummer();
        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));
        when(service.hentAktørId(kandidat.getFnr())).thenReturn(kandidat.getAktørId());
        settClusternavnIKandidatController("dev-fss");

        controller.hentKandidat(kandidat.getFnr());
    }

    @Test(expected = NotFoundException.class)
    public void hentKandidat__med_syntetisk_fnr_skal_kaste_exception_i_prod() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidatMedSyntetiskFødselsnummer();
        settClusternavnIKandidatController("prod-fss");

        var respons = controller.hentKandidat(kandidat.getFnr());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
        when(service.hentNyesteKandidat(aktørId)).thenReturn(Optional.of(enKandidat()));

        controller.hentKandidat(aktørId);

        verify(tilgangskontroll, times(1)).sjekkLesetilgangTilKandidat(aktørId);
    }

    @Test
    public void slettKandidat__skal_sjekke_skrivetilgang() {
        værInnloggetSom(enVeileder());
        String aktørId = "1000000000001";

        controller.slettKandidat(aktørId);
        verify(tilgangskontroll, times(1)).sjekkSkrivetilgangTilKandidat(aktørId);
    }

    @Test
    public void slettKandidat__skal_returnere_ok() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();

        when(service.slettKandidat(kandidat.getAktørId(), veileder)).thenReturn(Optional.of(1));
        ResponseEntity respons = controller.slettKandidat(kandidat.getAktørId());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void slettKandidat__skal_fungere_med_kun_fnr() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        String fnr = etFnr();
        String aktørId = enAktørId();

        when(service.hentAktørId(fnr)).thenReturn(aktørId);
        when(service.slettKandidat(aktørId, veileder)).thenReturn(Optional.of(1));

        ResponseEntity respons = controller.slettKandidat(fnr);
        verify(service).slettKandidat(aktørId, veileder);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void slettKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_finnes() {
        værInnloggetSom(enVeileder());
        String uregistrertAktørId = "1000000000001";

        ResponseEntity respons = controller.slettKandidat(uregistrertAktørId);
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tilgangskontroll.hentInnloggetVeileder()).thenReturn(veileder);
    }

    private void settClusternavnIKandidatController(String clusternavn) {
        ReflectionTestUtils.setField(controller, "clusternavn", clusternavn);
        controller.konfigurerFødselsnummerValidator();
    }

    private void resetClusternavnIKandidatController() {
        ReflectionTestUtils.setField(controller, "clusternavn", System.getenv("NAIS_CLUSTER_NAME"));
    }
}
