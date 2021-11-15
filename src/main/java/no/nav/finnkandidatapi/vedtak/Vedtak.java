package no.nav.finnkandidatapi.vedtak;

import lombok.Builder;
import lombok.Data;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakRad;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;

import java.time.LocalDateTime;

@Data
@Builder
public class Vedtak {

    private Long id;
    private String aktørId;
    private String fnr;
    private Long vedtakId;
    private Long personId;
    private String typeKode;
    private String statusKode;
    private String utfallKode;
    private String rettighetKode;
    private LocalDateTime fraDato;
    private LocalDateTime tilDato;
    private LocalDateTime arenaDbTidsstempel;
    private String arenaDbTransactionlogPosisjon;
    private String arenaDbOperasjon;
    private boolean slettet;

    public static Vedtak opprettFraAfter(String aktørId, VedtakReplikert vedtakEndret) {
        VedtakRad rad = vedtakEndret.getAfter();
        return opprett(aktørId, vedtakEndret, rad, false);
    }

    public static Vedtak opprettFraBefore(String aktørId, VedtakReplikert vedtakEndret) {

        VedtakRad rad = vedtakEndret.getBefore();
        return opprett(aktørId, vedtakEndret, rad, true);
    }

    private static Vedtak opprett(String aktørId, VedtakReplikert vedtakReplikert, VedtakRad rad, boolean slettet) {
        return Vedtak.builder()
                .aktørId(aktørId)
                .fnr(vedtakReplikert.getFodselsnr())
                .vedtakId(rad.getVedtak_id())
                .personId(rad.getPerson_id())
                .typeKode(rad.getVedtaktypekode())
                .statusKode(rad.getVedtakstatuskode())
                .utfallKode(rad.getUtfallkode())
                .rettighetKode(rad.getRettighetkode())
                .fraDato(rad.getFra_dato())
                .tilDato(rad.getTil_dato())
                .arenaDbTidsstempel(vedtakReplikert.getOp_ts())
                .arenaDbTransactionlogPosisjon(vedtakReplikert.getPos())
                .arenaDbOperasjon(vedtakReplikert.getOp_type())
                .slettet(slettet)
                .build();
    }

    public boolean erGyldig() {
        if (riktigVedtakTypeKode()
                && riktigUtfall()
                && riktigVedtakStatusKode()) {
            return true;
        }
        return false;
    }

    private boolean riktigVedtakStatusKode() {
        return statusKode != null
                && statusKode.equalsIgnoreCase("IVERK");
    }

    private boolean riktigUtfall() {
        return utfallKode != null
                && utfallKode.equalsIgnoreCase("JA");
    }

    private boolean riktigVedtakTypeKode() {
        return typeKode != null
                && (typeKode.equalsIgnoreCase("E")
                || typeKode.equalsIgnoreCase("G")
                || typeKode.equalsIgnoreCase("O"));
    }

    public boolean erPermittert() {
        return rettighetKode != null
                && (rettighetKode.equalsIgnoreCase("PERM")
                || rettighetKode.equalsIgnoreCase("FISK"));
    }
}
