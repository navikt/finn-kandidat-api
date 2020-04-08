package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class SjekkPermittertUtilTest {

    @Test
    public void skal_få_permittert_false_når_hverken_permittert_arbeidssoker_eller_permittert_vedtak() {
        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.empty(), Optional.empty());
        assertThat(erPermittert).isFalse();
    }

    @Test
    public void skal_få_permittert_false_når_permittert_arbeidssoker_med_feil_status_og_ikke_permittert_vedtak() {
        PermittertArbeidssoker ikkePermittertArbeidssoker = enPermittertArbeidssoker();
        ikkePermittertArbeidssoker.setStatusFraVeilarbRegistrering("SAGT_OPP");

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.of(ikkePermittertArbeidssoker), Optional.empty());
        assertThat(erPermittert).isFalse();
    }

    @Test
    public void skal_få_permittert_false_når_permittert_arbeidssoker_med_feil_status_og_vedtak_med_feil_rettighet() {
        PermittertArbeidssoker ikkePermittertArbeidssoker = enPermittertArbeidssoker();
        ikkePermittertArbeidssoker.setStatusFraVeilarbRegistrering("SAGT_OPP");
        Vedtak ikkePermittertVedtak = etVedtak();
        ikkePermittertVedtak.setRettighetKode("LONN");

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.of(ikkePermittertArbeidssoker), Optional.of(ikkePermittertVedtak));
        assertThat(erPermittert).isFalse();
    }

    @Test
    public void skal_få_permittert_false_når_ingen_permittert_arbeidssoker_og_vedtak_med_feil_rettighet() {
        Vedtak ikkePermittertVedtak = etVedtak();
        ikkePermittertVedtak.setRettighetKode("LONN");

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.empty(), Optional.of(ikkePermittertVedtak));
        assertThat(erPermittert).isFalse();
    }

    @Test
    public void skal_få_permittert_false_når_permittert_arbeidssoker_med_feil_status_er_nyere_enn_vedtak_med_riktig_rettighet() {
        PermittertArbeidssoker ikkePermittertArbeidssoker = enPermittertArbeidssoker();
        ikkePermittertArbeidssoker.setStatusFraVeilarbRegistrering("SAGT_OPP");
        Vedtak permittertVedtak = etVedtak();
        permittertVedtak.setFraDato(ikkePermittertArbeidssoker.getTidspunktForStatusFraVeilarbRegistrering().minusSeconds(1));

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.of(ikkePermittertArbeidssoker), Optional.of(permittertVedtak));
        assertThat(erPermittert).isFalse();
    }

    @Test
    public void skal_få_permittert_true_når_permittert_arbeidssoker_med_feil_status_er_eldre_enn_vedtak_med_riktig_rettighet() {
        PermittertArbeidssoker ikkePermittertArbeidssoker = enPermittertArbeidssoker();
        Vedtak permittertVedtak = etVedtak();
        ikkePermittertArbeidssoker.setStatusFraVeilarbRegistrering("SAGT_OPP");
        ikkePermittertArbeidssoker.setTidspunktForStatusFraVeilarbRegistrering(permittertVedtak.getFraDato().minusSeconds(1));

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.of(ikkePermittertArbeidssoker), Optional.of(permittertVedtak));
        assertThat(erPermittert).isTrue();
    }

    @Test
    public void skal_få_permittert_true_når_permittert_arbeidssoker_med_riktig_status_og_vedtak_med_feil_rettighet() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        Vedtak ikkePermittertVedtak = etVedtak();
        ikkePermittertVedtak.setRettighetKode("LONN");

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.of(permittertArbeidssoker), Optional.of(ikkePermittertVedtak));
        assertThat(erPermittert).isTrue();
    }

    @Test
    public void skal_få_permittert_true_når_permittert_arbeidssoker_med_riktig_status_og_vedtak_med_riktig_rettighet() {
        PermittertArbeidssoker permittertArbeidssoker = enPermittertArbeidssoker();
        Vedtak permittertVedtak = etVedtak();

        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(Optional.of(permittertArbeidssoker), Optional.of(permittertVedtak));
        assertThat(erPermittert).isTrue();
    }

}