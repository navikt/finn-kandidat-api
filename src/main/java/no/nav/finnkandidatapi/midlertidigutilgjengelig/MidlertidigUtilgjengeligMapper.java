package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligRepository.*;

@Component
public class MidlertidigUtilgjengeligMapper implements RowMapper<MidlertidigUtilgjengelig> {
    @Override
    public MidlertidigUtilgjengelig mapRow(ResultSet rs, int i) throws SQLException {
        return mapUtilgjengelig(rs, i);
    }

    private MidlertidigUtilgjengelig mapUtilgjengelig(ResultSet rs, int i) throws SQLException {
        return MidlertidigUtilgjengelig.builder()
                .aktørId(rs.getString(AKTØR_ID))
                .fraDato(tilLocalDateTime(rs.getTimestamp(FRA_DATO)))
                .tilDato(tilLocalDateTime(rs.getTimestamp(TIL_DATO)))
                .registrertAvIdent(rs.getString(REGISTRERT_AV_IDENT))
                .registrertAvNavn(rs.getString(REGISTRERT_AV_NAVN))
                .sistEndretAvIdent(rs.getString(SIST_ENDRET_AV_IDENT))
                .sistEndretAvNavn(rs.getString(SIST_ENDRET_AV_NAVN))
                .build();
    }

    private LocalDateTime tilLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
