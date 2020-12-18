package no.nav.finnkandidatapi.samtykke;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Op;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SamtykkeService {

    SamtykkeRepository samtykkeRepository;

    public SamtykkeService(SamtykkeRepository samtykkeRepository) {
        this.samtykkeRepository = samtykkeRepository;
    }

    public void behandleSamtykke(SamtykkeMelding samtykkeMelding) {
        if ("CV_HJEMMEL".equals(samtykkeMelding.getRessurs())) {
            validerSamtykkeMelding(samtykkeMelding);
            if ("SAMTYKKE_SLETTET".equals(samtykkeMelding.getMeldingType())) {
                slettCvSamtykke(samtykkeMelding);
            } else if ("SAMTYKKE_OPPRETTET".equals(samtykkeMelding.getMeldingType())) {
                opprettCvSamtykke(samtykkeMelding);
            }
        }
    }


    private void opprettCvSamtykke(SamtykkeMelding samtykkeMelding) {

        log.info("Lagrer samtykke" + " " + samtykkeMelding.getMeldingType());
        Optional<Samtykke> hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykkeMelding.getFnr());

        Samtykke samtykke = mapOpprettSamtykke(samtykkeMelding);
        hentetSamtykke.ifPresentOrElse(s -> {
            if(mottattMeldingErNyere(s, samtykke.getOpprettetTidspunkt())) {
                samtykkeRepository.oppdaterGittSamtykke(s);
                log.info("Oppdaterer samtykke");
            }
        }, () -> {
            samtykkeRepository.lagreSamtykke(samtykke);
            log.info("Nytt samtykke lagres");
        });
    }

    private void validerSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        if (StringUtils.isBlank(samtykkeMelding.getFnr())) {
            throw new RuntimeException("Fødselsnummer mangler");
        }

        if (StringUtils.isBlank(samtykkeMelding.getMeldingType())) {
            throw new RuntimeException("Meldingtype mangler");
        }

        if (StringUtils.isBlank(samtykkeMelding.getRessurs())) {
            throw new RuntimeException("Ressurs mangler");
        }

        if (samtykkeMelding.getOpprettetDato() == null && samtykkeMelding.getSlettetDato() == null) {
            throw new RuntimeException("OpprettetDato eller Slettetdato må ha verdi");
        }
    }

    private void slettCvSamtykke(SamtykkeMelding samtykkeMelding) {
        Optional<Samtykke> hentetSamtykke = samtykkeRepository.hentSamtykkeForCV(samtykkeMelding.getFnr());
        hentetSamtykke.filter(s -> mottattMeldingErNyere(s, samtykkeMelding.getSlettetDato()))
                .ifPresent(s -> {
                    samtykkeRepository.slettSamtykkeForCV(samtykkeMelding.getFnr());
                    log.info("Sletter gammelt samtykke");
                });
    }

    private boolean mottattMeldingErNyere(Samtykke hentetSamtykke, LocalDateTime opprettetTidspunkt) {
        return opprettetTidspunkt.isAfter(hentetSamtykke.getOpprettetTidspunkt());
    }

    private static Samtykke mapOpprettSamtykke(SamtykkeMelding samtykkeMelding) {
        String aktoerId = hentAlleTallFraString(samtykkeMelding.getFnr());

        int korrektLengdeAktoerId = 11;
        if (aktoerId.length() != korrektLengdeAktoerId) {
            throw new RuntimeException("Fnr må ha 13 tegn :" + samtykkeMelding.getFnr());
        }
        return new Samtykke(aktoerId, samtykkeMelding.getRessurs(), samtykkeMelding.getMeldingType(), samtykkeMelding.getOpprettetDato());
    }

    private static String hentAlleTallFraString(String stringMedTall) {
        return stringMedTall.replaceAll("\\D+", "");
    }

}
