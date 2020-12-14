package no.nav.finnkandidatapi.samtykke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class SamtykkeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final SamtykkeMapper samtykkeMapper;

    static final String SAMTYKKE_TABELL = "samtykke";
    private final String AKTOER_ID = "aktor_id";
    private final String ENDRING = "endring";
    private final String GJELDER = "gjelder";
    private final String SAMTYKKE_CV = "CV_HJEMMEL";

    @Autowired
    public SamtykkeRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert jdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = jdbcInsert
                .withTableName(SAMTYKKE_TABELL);
        samtykkeMapper = new SamtykkeMapper();
    }

    public void lagreEllerOppdaterSamtykke(Samtykke samtykke) {
        String update = String.format("UPDATE " + SAMTYKKE_TABELL +
                        " SET " + ENDRING + " = '%s'" +
                        " WHERE " + AKTOER_ID + "= '%s' AND " + GJELDER + "= '%s';",
                samtykke.getEndring(),
                samtykke.getAktoerId(),
                samtykke.getGjelder());

        int raderOppdatert = jdbcTemplate.update(update);

        if (raderOppdatert == 0) {
            Map<String, Object> samtykkeProps = mapTilDatabaseParametre(samtykke);
            jdbcInsert.execute(samtykkeProps);
        }
    }

    public boolean harSamtykkeForCV(String aktoerId) {
        try {
            Samtykke samtykke = jdbcTemplate.queryForObject("SELECT * from " + SAMTYKKE_TABELL +
                            " where " + AKTOER_ID + " = ? and " + GJELDER + " = '" + SAMTYKKE_CV + "'",
                    new Object[]{aktoerId}, samtykkeMapper);
            return samtykke != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    List<Samtykke> hentAlleSamtykker() {
        return jdbcTemplate.query("SELECT * FROM " + SAMTYKKE_TABELL, samtykkeMapper);
    }

    void slettAlleSamtykker() {
        jdbcTemplate.execute("DELETE FROM " + SAMTYKKE_TABELL);
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
