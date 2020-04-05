package no.nav.finnkandidatapi.vedtak;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static no.nav.finnkandidatapi.vedtak.VedtakRepository.*;

@Component
public class VedtakMapper implements RowMapper<Vedtak> {

    @Override
    public Vedtak mapRow(ResultSet rs, int i) throws SQLException {

        LocalDateTime arenaDbTidsstempel = rs.getTimestamp(ARENADB_TS) == null ? null : rs.getTimestamp(ARENADB_TS).toLocalDateTime();
        LocalDateTime fraDato = rs.getTimestamp(FRA_DATO) == null ? null : rs.getTimestamp(FRA_DATO).toLocalDateTime();
        LocalDateTime tilDato = rs.getTimestamp(TIL_DATO) == null ? null : rs.getTimestamp(TIL_DATO).toLocalDateTime();

        return Vedtak.builder()
                .id(rs.getLong(ID))
                .aktørId(rs.getString(AKTØR_ID))
                .fnr(rs.getString(FNR))
                .vedtakId(rs.getLong(VEDTAK_ID))
                .sakId(rs.getLong(SAK_ID))
                .personId(rs.getLong(PERSON_ID))
                .typeKode(rs.getString(TYPEKODE))
                .statusKode(rs.getString(STATUSKODE))
                .utfallKode(rs.getString(UTFALLKODE))
                .rettighetKode(rs.getString(RETTIGHETKODE))
                .fraDato(fraDato)
                .tilDato(tilDato)
                .arenaDbTidsstempel(arenaDbTidsstempel)
                .arenaDbTransactionlogPosisjon(rs.getString(ARENADB_POS))
                .arenaDbOperasjon(rs.getString(ARENADB_OP))
                .slettet(rs.getBoolean(SLETTET))
                .build();

    }
}
