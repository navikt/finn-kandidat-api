package no.nav.finnkandidatapi.kandidat.migrerTilterreleggingsbehov;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.finnkandidatapi.kandidat.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@ProtectedWithClaims(issuer = "isso")
@RestController
@RequestMapping("/migrer")
public class MigreringController {

    private final KandidatRepository repository;

    public MigreringController(KandidatRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @GetMapping
    public ResponseEntity kjørMigrering() {
        List<Kandidat> kandidater = repository.hentKandidater();
        kandidater.forEach(this::migrer);
        return ResponseEntity.ok().build();
    }

    private void migrer(Kandidat kandidat) {
        Set<Arbeidstid> arbeidstid = kandidat.getArbeidstid();
        boolean arbeidstidSlettet = arbeidstid.removeAll(List.of(Arbeidstid.KAN_IKKE_JOBBE, Arbeidstid.HELTID));

        Set<Fysisk> fysisk = kandidat.getFysisk();
        boolean fysiskSlettet = fysisk.removeAll(List.of(Fysisk.SYN, Fysisk.HØRSEL, Fysisk.ANNET));

        Set<Arbeidshverdagen> arbeidshverdagen = kandidat.getArbeidshverdagen();
        boolean arbeidshverdagenSlettet = arbeidshverdagen.remove(Arbeidshverdagen.ANNET);

        Set<UtfordringerMedNorsk> utfordringerMedNorsk = kandidat.getUtfordringerMedNorsk();
        boolean utfordringerMedNorskSlettet = utfordringerMedNorsk.removeAll(List.of(UtfordringerMedNorsk.REGNING_OG_TALLFORSTÅELSE, UtfordringerMedNorsk.ANDRE_UTFORDRINGER));

        boolean harSlettetBehov = arbeidstidSlettet || fysiskSlettet || arbeidshverdagenSlettet || utfordringerMedNorskSlettet;


        boolean inneholderOpplæring = arbeidshverdagen.remove(Arbeidshverdagen.TILRETTELAGT_OPPLÆRING);
        if (inneholderOpplæring) {
            arbeidshverdagen.add(Arbeidshverdagen.OPPLÆRING);
        }
        boolean inneholderArbeidsoppgaver = arbeidshverdagen.remove(Arbeidshverdagen.TILRETTELAGTE_ARBEIDSOPPGAVER);
        if (inneholderArbeidsoppgaver) {
            arbeidshverdagen.add(Arbeidshverdagen.OPPGAVER);
        }
        boolean inneholderMentor = arbeidshverdagen.remove(Arbeidshverdagen.MENTOR);
        if (inneholderMentor) {
            arbeidshverdagen.add(Arbeidshverdagen.TETT_OPPFØLGING);
        }

        boolean inneholderSnakke = utfordringerMedNorsk.remove(UtfordringerMedNorsk.SNAKKE_NORSK);
        if (inneholderSnakke) {
            utfordringerMedNorsk.add(UtfordringerMedNorsk.SNAKKE);
        }
        boolean inneholderSkrive = utfordringerMedNorsk.remove(UtfordringerMedNorsk.SKRIVE_NORSK);
        if (inneholderSkrive) {
            utfordringerMedNorsk.add(UtfordringerMedNorsk.SKRIVE);
        }
        boolean inneholderLese = utfordringerMedNorsk.remove(UtfordringerMedNorsk.LESE_NORSK);
        if (inneholderLese) {
            utfordringerMedNorsk.add(UtfordringerMedNorsk.LESE);
        }

        boolean harEndretBehov = inneholderOpplæring || inneholderArbeidsoppgaver || inneholderMentor || inneholderSnakke || inneholderSkrive || inneholderLese;

        if (harSlettetBehov || harEndretBehov) {
            repository.lagreKandidat(kandidat, Brukertype.SYSTEM);
        }
    }
}
