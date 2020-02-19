package no.nav.tag.finnkandidatapi.kandidat.migrerTilterreleggingsbehov;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.tag.finnkandidatapi.kandidat.*;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity kjørMigrering() {
        List<Kandidat> kandidater = repository.hentKandidater();
        kandidater.forEach(this::migrer);
        return ResponseEntity.ok().build();
    }

    private void migrer(Kandidat kandidat) {
        Set<ArbeidstidBehov> arbeidstid = kandidat.getArbeidstidBehov();
        boolean arbeidstidSlettet = arbeidstid.removeAll(List.of(ArbeidstidBehov.KAN_IKKE_JOBBE, ArbeidstidBehov.HELTID));

        Set<FysiskBehov> fysisk = kandidat.getFysiskeBehov();
        boolean fysiskSlettet = fysisk.removeAll(List.of(FysiskBehov.SYN, FysiskBehov.HØRSEL, FysiskBehov.ANNET));

        Set<ArbeidsmiljøBehov> arbeidsmiljø = kandidat.getArbeidsmiljøBehov();
        boolean arbeidsmiljøSlettet = arbeidsmiljø.remove(ArbeidsmiljøBehov.ANNET);

        Set<GrunnleggendeBehov> grunnleggende = kandidat.getGrunnleggendeBehov();
        boolean grunnleggendeSlettet = grunnleggende.removeAll(List.of(GrunnleggendeBehov.REGNING_OG_TALLFORSTÅELSE, GrunnleggendeBehov.ANDRE_UTFORDRINGER));

        boolean harSlettetBehov = arbeidstidSlettet || fysiskSlettet || arbeidsmiljøSlettet || grunnleggendeSlettet;


        boolean inneholderOpplæring = arbeidsmiljø.remove(ArbeidsmiljøBehov.TILRETTELAGT_OPPLÆRING);
        if (inneholderOpplæring) {
            arbeidsmiljø.add(ArbeidsmiljøBehov.OPPLÆRING);
        }
        boolean inneholderArbeidsoppgaver = arbeidsmiljø.remove(ArbeidsmiljøBehov.TILRETTELAGTE_ARBEIDSOPPGAVER);
        if (inneholderArbeidsoppgaver) {
            arbeidsmiljø.add(ArbeidsmiljøBehov.OPPGAVER);
        }
        boolean inneholderMentor = arbeidsmiljø.remove(ArbeidsmiljøBehov.MENTOR);
        if (inneholderMentor) {
            arbeidsmiljø.add(ArbeidsmiljøBehov.TETT_OPPFØLGING);
        }

        boolean inneholderSnakke = grunnleggende.remove(GrunnleggendeBehov.SNAKKE_NORSK);
        if (inneholderSnakke) {
            grunnleggende.add(GrunnleggendeBehov.SNAKKE);
        }
        boolean inneholderSkrive = grunnleggende.remove(GrunnleggendeBehov.SKRIVE_NORSK);
        if (inneholderSkrive) {
            grunnleggende.add(GrunnleggendeBehov.SKRIVE);
        }
        boolean inneholderLese = grunnleggende.remove(GrunnleggendeBehov.LESE_NORSK);
        if (inneholderLese) {
            grunnleggende.add(GrunnleggendeBehov.LESE);
        }

        boolean harEndretBehov = inneholderOpplæring || inneholderArbeidsoppgaver || inneholderMentor || inneholderSnakke || inneholderSkrive || inneholderLese;

        if (harSlettetBehov || harEndretBehov) {
            repository.lagreKandidat(kandidat, Brukertype.SYSTEM);
        }
    }
}
