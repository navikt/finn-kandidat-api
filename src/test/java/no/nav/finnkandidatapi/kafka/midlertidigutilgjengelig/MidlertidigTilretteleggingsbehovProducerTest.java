package no.nav.finnkandidatapi.kafka.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MidlertidigTilretteleggingsbehovProducerTest {

    @Test
    public void lager_utilgjengelig_tag_om_person_er_utilgjengelig() {
        MidlertidigUtilgjengelig omToUker = MidlertidigUtilgjengelig.builder()
                .tilDato(LocalDateTime.now().plusWeeks(2)).build();
        Optional<String> filter = MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter(Optional.of(omToUker));
        assertThat(filter).isNotEmpty().get().isEqualTo(MidlertidigTilretteleggingsbehovProducer.MIDLERTIDIG_UTILGJENGELIG);
    }

    @Test
    public void lager_tilgjengelig_innen_en_uke_tag_om_det_er_mindre_enn_en_uke_igjen() {
        MidlertidigUtilgjengelig omToDager = MidlertidigUtilgjengelig.builder()
                .tilDato(LocalDateTime.now().plusDays(2)).build();
        Optional<String> filter = MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter(Optional.of(omToDager));
        assertThat(filter).isNotEmpty().get().isEqualTo(MidlertidigTilretteleggingsbehovProducer.MIDLERTIDIG_UTILGJENGELIG_1_UKE);
    }

    @Test
    public void lager_ingen_filter_om_det_mangler_tildato() {
        MidlertidigUtilgjengelig tomTildato = MidlertidigUtilgjengelig.builder()
                .tilDato(null).build();
        Optional<String> filter = MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter(Optional.of(tomTildato));
        assertThat(filter).isEmpty();
    }

    @Test
    public void lager_ingen_filter_om_det_mangler_data() {
        Optional<String> filter = MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter(Optional.empty());
        assertThat(filter).isEmpty();
    }

    @Test
    public void lager_ingen_filter_om_tildato_er_tilbake_i_tid() {
        MidlertidigUtilgjengelig tomTildato = MidlertidigUtilgjengelig.builder()
                .tilDato(LocalDateTime.now().minusDays(1)).build();
        Optional<String> filter = MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter(Optional.of(tomTildato));
        assertThat(filter).isEmpty();
    }





}