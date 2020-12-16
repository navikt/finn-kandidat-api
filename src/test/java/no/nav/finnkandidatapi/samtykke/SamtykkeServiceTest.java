package no.nav.finnkandidatapi.samtykke;

import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class SamtykkeServiceTest {

    @Mock
    SamtykkeRepository samtykkeRepositoryMock;
    SamtykkeService samtykkeService;

    @Before
    public void init() {
        samtykkeService = new SamtykkeService(samtykkeRepositoryMock);
    }

    @Test
    public void skalLagreSamtykkeDersomGjelderCVHjemmelOgSamtykkeIkkeFinnes() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "100001000000",
                null,
                "SAMTYKKE_OPPRETTET",
                "CV_HJEMMEL",
                LocalDateTime.now() ,
                null,
                null,
                null,
                null);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(any());
    }

    @Test
    public void skalIkkeLagreSamtykkeDersomDetIkkeGjelderCVHjemmel() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "100001000000",
                        null,
                "SAMTYKKE_OPPRETTET",
                "ARBEIDSGIVER",
                LocalDateTime.now(),
        null,
                null,
                null,
                null);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(any());
    }

    @Test
    public void skalOppdatereSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErOppretting() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "100001000000",
                null,
                "SAMTYKKE_OPPRETTET",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null,
                null,
                null,
                null);

        Samtykke gammelSamtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykkeMelding.getAktoerId()))
                .thenReturn(gammelSamtykke);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).oppdaterSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
    }

    @Test
    public void skalSletteSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErSletting() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "100001000000",
                null,
                "SAMTYKKE_SLETTET",
                "CV_HJEMMEL",
                null,
                LocalDateTime.now(),
                null,
                null,
                null);

        Samtykke gammelSamtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykkeMelding.getAktoerId()))
                .thenReturn(gammelSamtykke);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).slettSamtykkeForCV(samtykkeMelding.getAktoerId());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(any());

    }

    @Test
    public void skalIkkeOppdatereSamtykkeDersomGjelderCVHjemmelOgNyereSamtykkeFinnesAllerede() {
        SamtykkeMelding samtykke = new SamtykkeMelding(
                "100001000000",
                null,
                "SAMTYKKE_OPPRETTET",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null,
                null,
                null,
                null);

        Samtykke nyttSamtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().plusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykke.getAktoerId()))
                .thenReturn(nyttSamtykke);

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
    }
}