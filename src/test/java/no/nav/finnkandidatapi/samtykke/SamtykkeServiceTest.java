package no.nav.finnkandidatapi.samtykke;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

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
        Samtykke samtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now());

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).lagreSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(samtykke);
    }

    @Test
    public void skalIkkeLagreSamtykkeDersomDetIkkeGjelderCVHjemmel() {
        Samtykke samtykke = new Samtykke(
                "100001000000",
                "ARBEIDSGIVER",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now());

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(samtykke);
    }

    @Test
    public void skalOppdatereSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErOppretting() {
        Samtykke samtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now());

        Samtykke gammelSamtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykke.getAktoerId()))
                .thenReturn(gammelSamtykke);

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).oppdaterSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(samtykke);
    }

    @Test
    public void skalSletteSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErSletting() {
        Samtykke samtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_SLETTET",
                LocalDateTime.now());

        Samtykke gammelSamtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykke.getAktoerId()))
                .thenReturn(gammelSamtykke);

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).slettSamtykkeForCV(samtykke.getAktoerId());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(samtykke);

    }

    @Test
    public void skalIkkeOppdatereSamtykkeDersomGjelderCVHjemmelOgNyereSamtykkeFinnesAllerede() {
        Samtykke samtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now());

        Samtykke nyttSamtykke = new Samtykke(
                "100001000000",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().plusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykke.getAktoerId()))
                .thenReturn(nyttSamtykke);

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(samtykke);
    }
}