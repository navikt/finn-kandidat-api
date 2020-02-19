package no.nav.finnkandidatapi.tilbakemelding;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TilbakemeldingRepository {
    static final String TILBAKEMELDING_TABELL = "tilbakemelding";
    static final String TILBAKEMELDING = "tilbakemelding";
    static final String BEHOV = "behov";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;


    public TilbakemeldingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TILBAKEMELDING_TABELL);
    }

    public int lagreTilbakemelding(Tilbakemelding tilbakemelding) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(TILBAKEMELDING, tilbakemelding.getTilbakemelding());
        parameters.put(BEHOV, tilbakemelding.getBehov().name());

        return jdbcInsert.execute(parameters);
    }

    public void slettAlleTilbakemeldinger() {
        jdbcTemplate.execute("DELETE FROM " + TILBAKEMELDING_TABELL);
    }

    public List<Tilbakemelding> hentAlleTilbakemeldinger() {
        String sql = "select " + BEHOV + "," + TILBAKEMELDING + " from " + TILBAKEMELDING_TABELL;
        return jdbcTemplate.query(sql, new TilbakemeldingRowMapper());
    }

    private static final class TilbakemeldingRowMapper implements RowMapper<Tilbakemelding> {

        @Override
        public Tilbakemelding mapRow(ResultSet resultSet, int i) throws SQLException {
            return new Tilbakemelding(
                    Behov.valueOf(resultSet.getString(BEHOV)),
                    resultSet.getString(TILBAKEMELDING)
            );
        }
    }
}
