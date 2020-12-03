package no.nav.finnkandidatapi.permittert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.enPermittertArbeidssoker;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"local", "mock"})
@DirtiesContext
class SlettUtdatertePermitteringerControllerTest {

    @Autowired
    private PermittertArbeidssokerRepository permittertArbeidssokerRepository;

    @Autowired
    private SlettUtdatertePermitteringerController controller;

    @Test
    public void slettAlleUtdatertePermitteringer() {
        String aktørId = "1856024171652";
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørId);
        permittertArbeidssokerRepository.lagrePermittertArbeidssoker(permittertArbeidssoker);

        controller.slettAlleUtdatertePermitteringer();

        Optional<PermittertArbeidssoker> hentetPermittertArbeidssoker = permittertArbeidssokerRepository.hentNyestePermittertArbeidssoker(aktørId);
        assertThat(hentetPermittertArbeidssoker).isEmpty();
    }
}
