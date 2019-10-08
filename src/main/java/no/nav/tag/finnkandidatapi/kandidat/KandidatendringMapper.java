package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.Kandidatendring;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.*;

@Component
public class KandidatendringMapper implements RowMapper<Kandidatendring> {
    @Override
    public Kandidatendring mapRow(ResultSet rs, int i) throws SQLException {
        return new Kandidatendring(rs.getString(AKTØR_ID), rs.getBoolean(SLETTET));
    }
}
