package no.nav.finnkandidatapi.samtykke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Repository
public class SamtykkeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final SamtykkeMapper samtykkeMapper;

    static final String SAMTYKKE_TABELL = "samtykke";
    private final String AKTOER_ID = "aktor_id";
    private final String ENDRING = "endring";
    private final String GJELDER = "gjelder";

    @Autowired
    public SamtykkeRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert jdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = jdbcInsert;
        samtykkeMapper = new SamtykkeMapper();
    }

    public void lagreSamtykke(Samtykke samtykke) {
        Map<String, Object> databaseParametre = mapTilDatabaseParametre(samtykke);
//        jdbcTemplate.execute();
    }

    public Optional<Samtykke> hentSamtykke(String aktoerId, String samtykkeGjelder) {
        try {
            Samtykke samtykke = jdbcTemplate.queryForObject("SELECT * from " + SAMTYKKE_TABELL + " where " + AKTOER_ID + " = ? and " + GJELDER + " = ? LIMIT 1", new Object[]{aktoerId, samtykkeGjelder}, samtykkeMapper);
            return Optional.ofNullable(samtykke);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Map<String, Object> mapTilDatabaseParametre(Samtykke samtykke) {
        return Map.of(
                AKTOER_ID, samtykke.getAktoerId(),
                ENDRING, samtykke.getEndring(),
                GJELDER, samtykke.getGjelder()
        );
    }

    private class SamtykkeMapper implements RowMapper<Samtykke> {

        @Override
        public Samtykke mapRow(ResultSet rs, int i) throws SQLException {
            return Samtykke.builder()
                    .aktoerId(rs.getString(AKTOER_ID))
                    .endring(rs.getString(ENDRING))
                    .gjelder(rs.getString(GJELDER))
                    .build();
        }
    }
}
