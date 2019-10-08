package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.Kandidatoppdatering;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.*;

@Component
public class KandidatoppdateringMapper implements RowMapper<Kandidatoppdatering> {
    @Override
    public Kandidatoppdatering mapRow(ResultSet rs, int i) throws SQLException {
        return new Kandidatoppdatering(rs.getString(AKTØR_ID), rs.getBoolean(SLETTET));
    }
}
