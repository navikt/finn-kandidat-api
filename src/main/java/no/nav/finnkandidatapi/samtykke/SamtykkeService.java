package no.nav.finnkandidatapi.samtykke;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SamtykkeService {

    private SamtykkeRepository samtykkeRepository;

    public SamtykkeService(SamtykkeRepository samtykkeRepository) {
        this.samtykkeRepository = samtykkeRepository;
    }

    public void behandleSamtykke(SamtykkeMelding samtykkeMelding) {
        if ("CV_HJEMMEL".equals(samtykkeMelding.getRessurs())) {
            SamtykkeMeldingValidator.valider(samtykkeMelding);

            if ("SAMTYKKE_SLETTET".equals(samtykkeMelding.getMeldingType())) {
                slettCvSamtykke(samtykkeMelding);
            } else if ("SAMTYKKE_OPPRETTET".equals(samtykkeMelding.getMeldingType())) {
                opprettCvSamtykke(samtykkeMelding);
            }
        }
    }

    private void opprettCvSamtykke(SamtykkeMelding samtykkeMelding) {

        log.info("Lagrer samtykke" + " " + samtykkeMelding.getMeldingType());
        Optional<Samtykke> hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykkeMelding.getAktoerId());

        Samtykke samtykke = mapOpprettSamtykke(samtykkeMelding);
        hentetSamtykke.ifPresentOrElse(s -> {
            if (mottattMeldingErNyere(s, samtykke.getOpprettetTidspunkt())) {
                samtykkeRepository.oppdaterGittSamtykke(s);
                log.info("Oppdaterer samtykke");
            }
        }, () -> {
            samtykkeRepository.lagreSamtykke(samtykke);
            log.info("Nytt samtykke lagres");
        });
    }

    private void slettCvSamtykke(SamtykkeMelding samtykkeMelding) {
        Optional<Samtykke> hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykkeMelding.getAktoerId());
        hentetSamtykke.filter(s -> mottattMeldingErNyere(s, samtykkeMelding.getSlettetDato()))
                .ifPresent(s -> {
                    samtykkeRepository.slettSamtykkeForCV(samtykkeMelding.getAktoerId());
                    log.info("Sletter gammelt samtykke");
                });
    }

    private boolean mottattMeldingErNyere(Samtykke hentetSamtykke, LocalDateTime opprettetTidspunkt) {
        return opprettetTidspunkt.isAfter(hentetSamtykke.getOpprettetTidspunkt());
    }

    private static Samtykke mapOpprettSamtykke(SamtykkeMelding samtykkeMelding) {
        int korrektLengdeAktoerId = 11;
        if (samtykkeMelding.getAktoerId().length() != korrektLengdeAktoerId) {
            throw new RuntimeException("Aktør-ID må ha 13 tegn :" + samtykkeMelding.getAktoerId());
        }
        return new Samtykke(samtykkeMelding.getAktoerId(), samtykkeMelding.getAktoerId(), samtykkeMelding.getRessurs(), samtykkeMelding.getMeldingType(), samtykkeMelding.getOpprettetDato());
    }

}
