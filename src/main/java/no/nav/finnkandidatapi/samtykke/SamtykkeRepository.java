package no.nav.finnkandidatapi.samtykke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class SamtykkeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final SamtykkeMapper samtykkeMapper;

    static final String SAMTYKKE_TABELL = "samtykke";
    private final String AKTOER_ID = "aktor_id";
    private final String GJELDER = "gjelder";
    private final String OPPRETTET_TIDSPUNKT = "opprettet_tidspunkt";

    private final String SAMTYKKE_CV = "CV_HJEMMEL";

    @Autowired
    public SamtykkeRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert jdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = jdbcInsert
                .withTableName(SAMTYKKE_TABELL);
        samtykkeMapper = new SamtykkeMapper();
    }

    public Samtykke hentSamtykkeForCV(String aktoerId) {
        try {
            Samtykke samtykke = jdbcTemplate.queryForObject("SELECT * from " + SAMTYKKE_TABELL +
                            " where " + AKTOER_ID + " = ? and " + GJELDER + " = '" + SAMTYKKE_CV + "'",
                    new Object[]{aktoerId}, samtykkeMapper);
            return samtykke;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void oppdaterSamtykke(Samtykke samtykke) {
        String update = String.format("UPDATE " + SAMTYKKE_TABELL +
                        " SET " + OPPRETTET_TIDSPUNKT + " = timestamp(%s)" +
                        " WHERE " + AKTOER_ID + "= '%s' AND " + GJELDER + "= '%s';",
                samtykke.getOpprettetTidspunkt(),
                samtykke.getAktoerId(),
                samtykke.getGjelder());

        jdbcTemplate.update(update);
    }

    public void lagreSamtykke(Samtykke samtykke) {
        jdbcInsert.execute(mapTilDatabaseParametre(samtykke));
    }


    // TODO: Lagre opprettet dato, og forkast eldre instanser.
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
                GJELDER, samtykke.getGjelder(),
                OPPRETTET_TIDSPUNKT, samtykke.getOpprettetTidspunkt()
        );
    }

    public void slettSamtykkeForCV(String aktoerId) {
        String delete = String.format("DELETE FROM " + SAMTYKKE_TABELL + " where " + GJELDER + " = '%s' and " + AKTOER_ID + " = '%s'", SAMTYKKE_CV, aktoerId);
        jdbcTemplate.execute(delete);
    }

    private class SamtykkeMapper implements RowMapper<Samtykke> {

        @Override
        public Samtykke mapRow(ResultSet rs, int i) throws SQLException {

            return Samtykke.builder()
                    .aktoerId(rs.getString(AKTOER_ID))
                    .gjelder(rs.getString(GJELDER))
                    .opprettetTidspunkt(konverter(rs.getTimestamp(OPPRETTET_TIDSPUNKT)))
                    .build();
        }
    }

    private static ZonedDateTime konverter(Timestamp timestamp) {
        return timestamp != null
                ? ZonedDateTime.ofLocal(timestamp.toLocalDateTime(), ZoneId.of("Europe/Oslo"), null)
                : null;
    }
}
