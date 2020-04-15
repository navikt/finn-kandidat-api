package no.nav.finnkandidatapi.permittert;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.enPermittertArbeidssoker;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"local", "mock"})
class SlettUtdatertePermitteringerControllerTest {

    @Autowired
    private PermittertArbeidssokerRepository permittertArbeidssokerRepository;

    @Autowired
    private SlettUtdatertePermitteringerController controller;

    @Test
    void slettAlleUtdatertePermitteringer() {
        String aktørId = "1856024171652";
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørId);
        permittertArbeidssokerRepository.lagrePermittertArbeidssoker(permittertArbeidssoker);

        controller.slettAlleUtdatertePermitteringer();

        Optional<PermittertArbeidssoker> hentetPermittertArbeidssoker = permittertArbeidssokerRepository.hentNyestePermittertArbeidssoker(aktørId);
        assertThat(hentetPermittertArbeidssoker).isEmpty();
    }
}
