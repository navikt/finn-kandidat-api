package no.nav.finnkandidatapi.samtykke;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class SamtykkeRepositoryTest {

    // TODO: Test med ZonedDateTime

    @Autowired
    private SamtykkeRepository samtykkeRepository;

    @Before
    public void setup() {
        samtykkeRepository.slettAlleSamtykker();
    }

    @Test
    public void testLagringUthentingAvSamtykke() {
        String aktoerId = "232432532";
        String gjelder = "CV_HJEMMEL";
        String endring = "SAMTYKKE_OPPRETTET";
        ZonedDateTime opprettetTidspunkt = ZonedDateTime.now();

        Samtykke samtykke = new Samtykke(aktoerId, gjelder, endring, opprettetTidspunkt);
        samtykkeRepository.lagreSamtykke(samtykke);

        assertEquals(1, samtykkeRepository.hentAlleSamtykker().size());

        Samtykke lagretSamtykke = samtykkeRepository.hentAlleSamtykker().get(0);
        assertEquals(samtykke.getAktoerId(), lagretSamtykke.getAktoerId());
        assertEquals(samtykke.getGjelder(), lagretSamtykke.getGjelder());
        assertEquals(samtykke.getOpprettetTidspunkt(), lagretSamtykke.getOpprettetTidspunkt());

        assertTrue(samtykkeRepository.harSamtykkeForCV(aktoerId));
    }

    // Teste oppdatere samtykke

    @Test
    public void slettetSamtykkeForCV() {
        String aktoerId = "232432532";
        Samtykke samtykke = new Samtykke(aktoerId, "CV_HJEMMEL", "SAMTYKKE_SLETTET", ZonedDateTime.now());
        samtykkeRepository.oppdaterSamtykke(samtykke);

        assertFalse(samtykkeRepository.harSamtykkeForCV(aktoerId));
    }
}