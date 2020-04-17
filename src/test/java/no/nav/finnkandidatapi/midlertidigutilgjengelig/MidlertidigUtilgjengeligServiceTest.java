package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MidlertidigUtilgjengeligServiceTest {

    private MidlertidigUtilgjengeligService service;

    @Mock
    private MidlertidigUtilgjengeligRepository repository;

    Veileder enVeileder = new Veileder("A100000", "Ola Nordmann");

    @Before
    public void before() {
        service = new MidlertidigUtilgjengeligService(repository);
    }

    @Test
    public void hentMidlertidigUtilgjengelig__kan_hente_midlertidig_utilgjengelig() {

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("555555");

        when(repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        Optional<MidlertidigUtilgjengelig> response = service.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());
        assertThat(response).isNotEmpty().get().isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void postMidlertidigUtilgjengelig__kan_poste_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("77777777");

        MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto = new MidlertidigUtilgjengeligDto(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato());

        when(repository.lagreMidlertidigUtilgjengelig(any(MidlertidigUtilgjengelig.class)))
                .thenReturn(1);

        when(repository.hentMidlertidigUtilgjengeligMedId(1))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        MidlertidigUtilgjengelig response = service.opprettMidlertidigUtilgjengelig(midlertidigUtilgjengeligDto, enVeileder);

        assertThat(response).isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void deleteMidlertidigUtilgjengelig__kan_slette_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("32323232");

        when(repository.slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(1);

        service.slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());

        verify(repository, times(1)).slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());
    }

    @Test
    public void forlengeMidlertidigUtilgjengelig__kan_oppdatere_tildato() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("7777722");

        when(repository.forlengeMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder))
                .thenReturn(1);

        when(repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        var response = service.forlengeMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder);

        verify(repository, times(1)).forlengeMidlertidigUtilgjengelig(any(), any(), any());

        assertThat(response).isNotEmpty().get().isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void forlengeMidlertidigUtilgjengelig__tildato_kan_ikke_være_tilbake_i_tid() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("7777722");
        midlertidigUtilgjengelig.setTilDato(LocalDateTime.of(2000, 1, 1, 1, 0, 0));

        when(repository.forlengeMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder))
                .thenReturn(1);

        when(repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));
        
        assertThrows(BadRequestException.class,
                () -> {
                    service.forlengeMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder);
                });
    }
}