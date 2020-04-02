package no.nav.finnkandidatapi.permittert;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static no.nav.finnkandidatapi.permittert.PermittertArbeidssokerRepository.*;

@Component
public class PermittertArbeidssokerMapper implements RowMapper<PermittertArbeidssoker> {

    @Override
    public PermittertArbeidssoker mapRow(ResultSet rs, int i) throws SQLException {
        if (rs.getBoolean(SLETTET)) {
            return null;
        }

        return mapPermittertArbeidssoker(rs, i);
    }

    public static PermittertArbeidssoker mapPermittertArbeidssoker(ResultSet rs, int i) throws SQLException {
        LocalDateTime tidspunktForStatusFraVeilarb = rs.getTimestamp(TIDSPUNKT_FOR_STATUS_FRA_VEILARB) == null ? null : rs.getTimestamp(TIDSPUNKT_FOR_STATUS_FRA_VEILARB).toLocalDateTime();

        return PermittertArbeidssoker.builder()
                .id(rs.getInt(ID))
                .aktørId(rs.getString(AKTØR_ID))
                .statusFraVeilarbRegistrering(rs.getString(STATUS_FRA_VEILARB_REGISTRERING))
                .tidspunktForStatusFraVeilarbRegistrering(tidspunktForStatusFraVeilarb)
                .build();
    }
}