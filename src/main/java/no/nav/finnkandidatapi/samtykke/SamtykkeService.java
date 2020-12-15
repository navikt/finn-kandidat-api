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
            samtykkeRepository.lagreEllerOppdaterSamtykke(samtykke);
        }

    }

    private boolean skalBeholdeEksisterendeSamtykke(Samtykke samtykke, Samtykke eksisterendeSamtykke) {
        return samtykke.getOpprettetTidspunkt().isBefore(eksisterendeSamtykke.getOpprettetTidspunkt());
    }
}
