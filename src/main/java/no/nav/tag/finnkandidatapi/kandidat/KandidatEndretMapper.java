package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.KandidatEndret;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.*;

@Component
public class KandidatEndretMapper implements RowMapper<KandidatEndret> {
    @Override
    public KandidatEndret mapRow(ResultSet rs, int i) throws SQLException {
        return new KandidatEndret(rs.getString(AKTØR_ID), rs.getBoolean(SLETTET));
    }
}
