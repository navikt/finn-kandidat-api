package no.nav.finnkandidatapi.vedtak;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static no.nav.finnkandidatapi.vedtak.VedtakRepository.*;

@Component
public class VedtakMapper implements RowMapper<Vedtak> {

    private Long getNullableLong(ResultSet rs, String fieldName) throws SQLException {
        Long value = rs.getLong(fieldName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String fieldName) throws SQLException {
        return rs.getTimestamp(fieldName) == null ? null : rs.getTimestamp(fieldName).toLocalDateTime();
    }

    @Override
    public Vedtak mapRow(ResultSet rs, int i) throws SQLException {

        return Vedtak.builder()
                .id(rs.getLong(ID))
                .aktørId(rs.getString(AKTØR_ID))
                .fnr(rs.getString(FNR))
                .vedtakId(getNullableLong(rs, VEDTAK_ID))
                .personId(getNullableLong(rs, PERSON_ID))
                .typeKode(rs.getString(TYPEKODE))
                .statusKode(rs.getString(STATUSKODE))
                .utfallKode(rs.getString(UTFALLKODE))
                .rettighetKode(rs.getString(RETTIGHETKODE))
                .fraDato(getLocalDateTime(rs, FRA_DATO))
                .tilDato(getLocalDateTime(rs, TIL_DATO))
                .arenaDbTidsstempel(getLocalDateTime(rs, ARENADB_TS))
                .arenaDbTransactionlogPosisjon(rs.getString(ARENADB_POS))
                .arenaDbOperasjon(rs.getString(ARENADB_OP))
                .slettet(rs.getBoolean(SLETTET))
                .build();
    }
}
