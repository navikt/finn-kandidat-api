package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KandidatService {

    public void oppdaterKandidat(Kandidat kandidat, Veileder innloggetVeileder) {
        kandidat.setSistEndretAv(innloggetVeileder.getNavIdent());
        kandidat.setSistEndret(LocalDateTime.now());
    }

}
