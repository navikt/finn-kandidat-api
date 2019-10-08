package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.HarTilretteleggingsbehov;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.*;

@Component
public class HarTilretteleggingsbehovMapper implements RowMapper<HarTilretteleggingsbehov> {
    @Override
    public HarTilretteleggingsbehov mapRow(ResultSet rs, int i) throws SQLException {
        return new HarTilretteleggingsbehov(rs.getString(AKTØR_ID), rs.getBoolean(SLETTET));
    }
}
