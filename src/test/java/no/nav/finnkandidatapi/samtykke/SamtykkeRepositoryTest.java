package no.nav.finnkandidatapi.samtykke;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.finnkandidatapi.TestData.now;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class SamtykkeRepositoryTest {

    @Autowired
    private SamtykkeRepository samtykkeRepository;

    @Before
    public void setup() {
        samtykkeRepository.slettAlleSamtykker();
    }

    @Test
    public void skalKunneLagreOgHenteUtSamtykke() {
        String aktoerId = "232432532";
        String foedselsnummer = "17051422877";
        String gjelder = "CV_HJEMMEL";
        String endring = "SAMTYKKE_OPPRETTET";
        LocalDateTime opprettetTidspunkt = now();

        Samtykke samtykke = new Samtykke(aktoerId, foedselsnummer, gjelder, endring, opprettetTidspunkt);
        samtykkeRepository.lagreSamtykke(samtykke);

        List<Samtykke> samtykker = samtykkeRepository.hentAlleSamtykker();
        assertEquals(1, samtykker.size());

        Samtykke lagretSamtykke = samtykker.get(0);
        assertEquals(samtykke.getFoedselsnummer(), lagretSamtykke.getFoedselsnummer());
        assertEquals(samtykke.getGjelder(), lagretSamtykke.getGjelder());
        assertEquals(samtykke.getOpprettetTidspunkt(), lagretSamtykke.getOpprettetTidspunkt());

        assertTrue(samtykkeRepository.hentSamtykkeForCV(aktoerId).isPresent());
    }

    @Test
    public void skalKunneLagreOgHenteUtSamtykkeMedFnrNull() {
        String aktoerId = "232432532";
        String foedselsnummer = null;
        String gjelder = "CV_HJEMMEL";
        String endring = "SAMTYKKE_OPPRETTET";
        LocalDateTime opprettetTidspunkt = now();

        Samtykke samtykke = new Samtykke(aktoerId, foedselsnummer, gjelder, endring, opprettetTidspunkt);
        samtykkeRepository.lagreSamtykke(samtykke);

        List<Samtykke> samtykker = samtykkeRepository.hentAlleSamtykker();
        assertEquals(1, samtykker.size());

        Samtykke lagretSamtykke = samtykker.get(0);
        assertEquals(samtykke.getFoedselsnummer(), lagretSamtykke.getFoedselsnummer());
        assertEquals(samtykke.getGjelder(), lagretSamtykke.getGjelder());
        assertEquals(samtykke.getOpprettetTidspunkt(), lagretSamtykke.getOpprettetTidspunkt());

        assertTrue(samtykkeRepository.hentSamtykkeForCV(aktoerId).isPresent());
    }

    @Test
    public void skalKunneOppdatereSamtykke() {
        LocalDateTime opprettetTidspunkt = now().minusDays(1);
        LocalDateTime opprettetTidspunktOppdatert = opprettetTidspunkt.plusDays(1);

        String aktoerId = "232432532";
        String foedselsnummer = "17051422877";
        String gjelder = "CV_HJEMMEL";
        String endring = "SAMTYKKE_OPPRETTET";

        samtykkeRepository.lagreSamtykke(
                new Samtykke(aktoerId, foedselsnummer, gjelder, endring, opprettetTidspunkt));

        samtykkeRepository.oppdaterGittSamtykke(
                new Samtykke(aktoerId, foedselsnummer, gjelder, endring, opprettetTidspunktOppdatert));
        List<Samtykke> samtykker = samtykkeRepository.hentAlleSamtykker();
        assertEquals(1, samtykkeRepository.hentAlleSamtykker().size());

        Samtykke oppdatertSamtykke = samtykker.get(0);
        assertEquals(aktoerId, oppdatertSamtykke.getAktorId());
        assertEquals(gjelder, oppdatertSamtykke.getGjelder());
        assertTrue(opprettetTidspunktOppdatert.isEqual(oppdatertSamtykke.getOpprettetTidspunkt()));
    }

    @Test
    public void skalKunneSletteSamtykke() {
        LocalDateTime opprettetTidspunkt = LocalDateTime.now().minusDays(1);
        LocalDateTime opprettetTidspunktOppdatert = opprettetTidspunkt.plusDays(1);

        String aktoerId = "232432532";
        String foedselsnummer = "17051422877";
        String gjelder = "CV_HJEMMEL";
        String endring = "SAMTYKKE_OPPRETTET";

        samtykkeRepository.lagreSamtykke(
                new Samtykke(aktoerId, foedselsnummer, gjelder, endring, opprettetTidspunkt));

        samtykkeRepository.slettSamtykkeForCV(aktoerId);
        List<Samtykke> samtykker = samtykkeRepository.hentAlleSamtykker();
        assertEquals(0, samtykkeRepository.hentAlleSamtykker().size());
    }

    @Test
    public void slettetSamtykkeForCV() {
        String aktoerId = "232432532";
        String foedselsnummer = "17051422877";

        Samtykke samtykke = new Samtykke(aktoerId, foedselsnummer, "CV_HJEMMEL", "SAMTYKKE_SLETTET", LocalDateTime.now());
        samtykkeRepository.oppdaterGittSamtykke(samtykke);

        assertFalse(samtykkeRepository.hentSamtykkeForCV(aktoerId).isPresent());
    }
}
