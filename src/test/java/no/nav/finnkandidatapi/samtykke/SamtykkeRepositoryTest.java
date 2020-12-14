package no.nav.finnkandidatapi.samtykke;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
    public void testHarSamtykkeForCV() {
        String aktoerId = "232432532";
        String gjelder = "CV_HJEMMEL";
        String endring = "Samtykke opprettet";

        Samtykke samtykke = new Samtykke(aktoerId, gjelder, endring);
        samtykkeRepository.lagreEllerOppdaterSamtykke(samtykke);

        assertEquals(1, samtykkeRepository.hentAlleSamtykker().size());

        Samtykke lagretSamtykke = samtykkeRepository.hentAlleSamtykker().get(0);
        assertEquals(samtykke.getAktoerId(), lagretSamtykke.getAktoerId());
        assertEquals(samtykke.getEndring(), lagretSamtykke.getEndring());
        assertEquals(samtykke.getGjelder(), lagretSamtykke.getGjelder());

        assertTrue(samtykkeRepository.harSamtykkeForCV(aktoerId));
    }

    @Test
    public void harLagretSamtykkeMenIkkeForCV() {
        String aktoerId = "232432532";
        String gjelder = "IKKE_CV_HJEMMEL";
        String endring = "Samtykke opprettet";

        Samtykke samtykke = new Samtykke(aktoerId, gjelder, endring);
        samtykkeRepository.lagreEllerOppdaterSamtykke(samtykke);

        assertEquals(1, samtykkeRepository.hentAlleSamtykker().size());
        Assert.assertFalse(samtykkeRepository.harSamtykkeForCV(aktoerId));
    }

    @Test
    public void nyttSamtykkeForSammePersonOppdatererMenLagrerIkkeNytt() {
        String aktoerId = "232432532";
        String gjelder = "CV_HJEMMEL";
        String endring = "Samtykke opprettet";

        Samtykke samtykke = new Samtykke(aktoerId, gjelder, endring);
        samtykkeRepository.lagreEllerOppdaterSamtykke(samtykke);

        String nyEndring = "Samtykke avsluttet";
        Samtykke nyttSamtykke = new Samtykke(aktoerId, gjelder, nyEndring);
        samtykkeRepository.lagreEllerOppdaterSamtykke(nyttSamtykke);

        assertEquals(1, samtykkeRepository.hentAlleSamtykker().size());
    }

    @Test
    public void samtykkeForToUlikePersonerGjørAtToSamtykkerBlirLagret() {
        String aktoerId = "232432532";
        String gjelder = "CV_HJEMMEL";
        String endring = "Samtykke opprettet";

        Samtykke samtykke = new Samtykke(aktoerId, gjelder, endring);
        samtykkeRepository.lagreEllerOppdaterSamtykke(samtykke);

        String annenAktoerId = "897632532";
        Samtykke annetSamtykke = new Samtykke(annenAktoerId, gjelder, endring);
        samtykkeRepository.lagreEllerOppdaterSamtykke(annetSamtykke);

        assertEquals(2, samtykkeRepository.hentAlleSamtykker().size());
    }
}