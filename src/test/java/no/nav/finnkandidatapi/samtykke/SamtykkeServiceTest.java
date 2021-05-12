package no.nav.finnkandidatapi.samtykke;

import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class SamtykkeServiceTest {

    @Mock
    SamtykkeRepository samtykkeRepositoryMock;
    SamtykkeService samtykkeService;
    private String aktorId = "1000100000100";

    @Before
    public void init() {
        samtykkeService = new SamtykkeService(samtykkeRepositoryMock);
    }

    @Test
    public void skalLagreSamtykkeDersomGjelderCVHjemmelOgSamtykkeIkkeFinnes() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                aktorId,
                "27075349594",
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
                aktorId,
                "27075349594",
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
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null);

        Samtykke gammeltSamtykke = new Samtykke(
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykkeMelding.getAktoerId()))
                .thenReturn(Optional.of(gammeltSamtykke));

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).oppdaterGittSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
    }

    @Test
    public void skalSletteSamtykkeDersomGjelderCVHjemmelOgGammeltSamtykkeFinnesOgErSletting() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                null,
                LocalDateTime.now()
        );

        Samtykke gammeltSamtykke = new Samtykke(
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().minusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykkeMelding.getAktoerId()))
                .thenReturn(Optional.of(gammeltSamtykke));

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).slettSamtykkeForCV(samtykkeMelding.getAktoerId());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());

    }

    @Test
    public void skalIkkeOppdatereSamtykkeDersomGjelderCVHjemmelOgNyereSamtykkeFinnesAllerede() {
        SamtykkeMelding samtykke = new SamtykkeMelding(
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                LocalDateTime.now(),
                null);

        Samtykke nyttSamtykke = new Samtykke(
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                "SAMTYKKE_OPPRETTET",
                LocalDateTime.now().plusDays(1)
        );

        Mockito.when(samtykkeRepositoryMock.hentSamtykkeForCV(samtykke.getAktoerId()))
                .thenReturn(Optional.of(nyttSamtykke));

        samtykkeService.behandleSamtykke(samtykke);
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).lagreSamtykke(any());
    }

    @Test
    public void skalLagreVeldigGammelDatoHvisDatoMangler() {
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(
                aktorId,
                "27075349594",
                "CV_HJEMMEL",
                null,
                null);

        samtykkeService.behandleSamtykke(samtykkeMelding);
        Mockito.verify(samtykkeRepositoryMock, Mockito.times(1)).lagreSamtykke(
                eq(new Samtykke(aktorId,"27075349594", "CV_HJEMMEL", "SAMTYKKE_OPPRETTET",
                        LocalDateTime.of(LocalDate.of(-999_999_999, 1, 1), LocalTime.of(0,0)))));
        Mockito.verify(samtykkeRepositoryMock, Mockito.never()).oppdaterGittSamtykke(any());
    }
}