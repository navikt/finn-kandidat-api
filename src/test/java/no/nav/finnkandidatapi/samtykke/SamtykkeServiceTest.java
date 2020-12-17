package no.nav.finnkandidatapi.samtykke;

import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

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
                "27075349594",
                "SAMTYKKE_OPPRETTET",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());
    }

    @Test
    public void skalIkkeLagreSamtykkeDersomDetIkkeGjelderCVHjemmel() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "27075349594",
                "SAMTYKKE_OPPRETTET",
                "ARBEIDSGIVER",
                LocalDateTime.now(),
                null);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());
    }

    @Test
    public void skalOppdatereSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErOppretting() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "27075349594",
                "SAMTYKKE_OPPRETTET",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null);

        Samtykke gammeltSamtykke = new Samtykke(
                "27075349594",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykkeMelding.getFnr()))
                .thenReturn(Optional.of(gammeltSamtykke));

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).oppdaterGittSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
    }

    @Test
    public void skalSletteSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErSletting() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                "27075349594",
                "SAMTYKKE_SLETTET",
                "CV_HJEMMEL",
                null,
                LocalDateTime.now()
                );

        Samtykke gammeltSamtykke = new Samtykke(
                "27075349594",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykkeMelding.getFnr()))
                .thenReturn(Optional.of(gammeltSamtykke));

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).slettSamtykkeForCV(samtykkeMelding.getFnr());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());

    }

    @Test
    public void skalIkkeOppdatereSamtykkeDersomGjelderCVHjemmelOgNyereSamtykkeFinnesAllerede() {
        SamtykkeMelding samtykke = new SamtykkeMelding(
                "27075349594",
                "SAMTYKKE_OPPRETTET",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null);

        Samtykke nyttSamtykke = new Samtykke(
                "27075349594",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().plusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykke.getFnr()))
                .thenReturn(Optional.of(nyttSamtykke));

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
    }
}