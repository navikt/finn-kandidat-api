package no.nav.finnkandidatapi.samtykke;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SamtykkeService {

    SamtykkeRepository samtykkeRepository;

    public SamtykkeService(SamtykkeRepository samtykkeRepository) {
        this.samtykkeRepository = samtykkeRepository;
    }

    public void behandleSamtykke(SamtykkeMelding samtykkeMelding) {
        if ("CV_HJEMMEL".equals(samtykkeMelding.getRessurs())) {
            if ("SAMTYKKE_SLETTET".equals(samtykkeMelding.getMeldingType())) {
                slettCvSamtykke(samtykkeMelding);
            } else if ("SAMTYKKE_OPPRETTET".equals(samtykkeMelding.getMeldingType())) {
                opprettCvSamtykke(samtykkeMelding);
            } else {
                //log ubehandlet melding
            }
        }
    }


    private void opprettCvSamtykke(SamtykkeMelding samtykkeMelding) {

        log.info("Lagrer samtykke" + " " + samtykkeMelding.getMeldingType());
        Samtykke hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykkeMelding.getAktoerId());

        Samtykke samtykke = mapOpprettSamtykke(samtykkeMelding);
        if (hentetSamtykke == null) {
            samtykkeRepository.lagreSamtykke(samtykke);
            log.info("Nytt samtykke");
        } else {
            if(samtykke.getOpprettetTidspunkt().isAfter(hentetSamtykke.getOpprettetTidspunkt())) {
                samtykkeRepository.oppdaterSamtykke(samtykke);
                log.info("Oppdaterer samtykke");
            } else {
                // log
            }
        }
    }

    private void slettCvSamtykke(SamtykkeMelding samtykkeMelding) {
            log.info("Lagrer samtykke" + " " + samtykkeMelding.getMeldingType());
            Samtykke hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykkeMelding.getAktoerId());
            if (hentetSamtykke == null) {
                // Gjør ingenting
                log.info("...");
            } else {
                if(samtykkeMelding.getSlettetDato().isAfter(hentetSamtykke.getOpprettetTidspunkt())) {
                    samtykkeRepository.slettSamtykkeForCV(samtykkeMelding.getAktoerId());
                    log.info("Sletter gammelt samtykke");
                } else {
                    // Gjør ingenting
                }
            }
    }

    private static Samtykke mapOpprettSamtykke(SamtykkeMelding samtykkeMelding) {
        String aktoerId = hentAlleTallFraString(samtykkeMelding.getAktoerId());

        int korrektLengdeAktoerId = 13;
        if (aktoerId.length() != korrektLengdeAktoerId) {
            throw new RuntimeException("AktørID må ha 13 tegn :" + samtykkeMelding.getAktoerId());
        }
        return new Samtykke(aktoerId, samtykkeMelding.getRessurs(), samtykkeMelding.getMeldingType(), samtykkeMelding.getOpprettetDato());
    }

    private static String hentAlleTallFraString(String stringMedTall) {
        return stringMedTall.replaceAll("\\D+", "");
    }

}
