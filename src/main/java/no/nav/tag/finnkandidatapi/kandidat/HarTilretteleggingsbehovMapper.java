package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.SLETTET;

@Component
public class HarTilretteleggingsbehovMapper implements RowMapper<HarTilretteleggingsbehov> {

    @Override
    public HarTilretteleggingsbehov mapRow(ResultSet rs, int i) throws SQLException {
        Kandidat kandidat = KandidatMapper.mapKandidat(rs, i);
        boolean harTilretteleggingsbehov = !rs.getBoolean(SLETTET);
        return new HarTilretteleggingsbehov(kandidat.getAktørId(), harTilretteleggingsbehov, kandidat.kategorier());
    }
}
