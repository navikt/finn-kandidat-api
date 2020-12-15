package no.nav.finnkandidatapi.samtykke;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SamtykkeService {

    SamtykkeRepository samtykkeRepository;

    public SamtykkeService(SamtykkeRepository samtykkeRepository) {
        this.samtykkeRepository = samtykkeRepository;
    }

    public void behandleSamtykke(Samtykke samtykke) {
        if ("CV_HJEMMEL".equals(samtykke.getGjelder())) {
            log.info("Lagrer samtykke" + " " + samtykke.getEndring());
            Samtykke hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykke.getAktoerId());
            if (hentetSamtykke == null) {
                samtykkeRepository.lagreSamtykke(samtykke);
                log.info("Nytt sam‡tykke");
            } else {
                håndterAtSamtykkeFinnesFraFør(samtykke, hentetSamtykke);
            }

        }
    }

    private void håndterAtSamtykkeFinnesFraFør(Samtykke samtykke, Samtykke eksisterendeSamtykke) {
        if (skalBeholdeEksisterendeSamtykke(samtykke, eksisterendeSamtykke)) {
            log.info("Oppdaterer ikke samtykke siden det finnes nyere samtykke");
            return;
        }

        if ("SAMTYKKE_SLETTET".equals(samtykke.getEndring())) {
            samtykkeRepository.slettSamtykkeForCV(samtykke.getAktoerId());
            log.info("Sletter gammelt samtykke");
        } else {
            samtykkeRepository.oppdaterSamtykke(samtykke);
            log.info("Oppdaterer samtykke");
        }
    }

    private boolean skalBeholdeEksisterendeSamtykke(Samtykke samtykke, Samtykke eksisterendeSamtykke) {
        return samtykke.getOpprettetTidspunkt().isBefore(eksisterendeSamtykke.getOpprettetTidspunkt());
    }
}
