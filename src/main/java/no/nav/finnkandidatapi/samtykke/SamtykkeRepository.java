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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SamtykkeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final SamtykkeMapper samtykkeMapper;

    static final String ID = "id";
    static final String AKTOER_ID = "aktor_id";
    static final String SAMTYKKE_TABELL = "samtykke";
    private final String FOEDSELSNUMMER = "foedselsnummer";
    private final String GJELDER = "gjelder";
    private final String OPPRETTET_TIDSPUNKT = "opprettet_tidspunkt";
    private final String ENDRING = "ENDRING";

    private final String SAMTYKKE_CV = "CV_HJEMMEL";

    @Autowired
    public SamtykkeRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert jdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = jdbcInsert
                .withTableName(SAMTYKKE_TABELL)
                .usingGeneratedKeyColumns(ID);
        samtykkeMapper = new SamtykkeMapper();
    }

    public Optional<Samtykke> hentSamtykkeForCV(String aktørId) {
        try {
            Samtykke samtykke = jdbcTemplate.queryForObject("SELECT * from " + SAMTYKKE_TABELL +
                            " where " + AKTOER_ID + " = ? and " + GJELDER + " = '" + SAMTYKKE_CV + "'",
                    new Object[]{aktørId}, samtykkeMapper);
            return Optional.ofNullable(samtykke);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void oppdaterGittSamtykke(Samtykke samtykke) {
        String update = "UPDATE " + SAMTYKKE_TABELL +
                " SET " + OPPRETTET_TIDSPUNKT + " = ?, " + ENDRING + " = ?" +
                " WHERE " + AKTOER_ID + "= ? AND " + GJELDER + "= ?;";

        jdbcTemplate.update(update,
                samtykke.getOpprettetTidspunkt(),
                samtykke.getEndring(),
                samtykke.getAktorId(),
                samtykke.getGjelder());
    }

    public void lagreSamtykke(Samtykke samtykke) {
        jdbcInsert.execute(mapTilDatabaseParametre(samtykke));
    }

    List<Samtykke> hentAlleSamtykker() {
        return jdbcTemplate.query("SELECT * FROM " + SAMTYKKE_TABELL, samtykkeMapper);
    }

    void slettAlleSamtykker() {
        jdbcTemplate.execute("DELETE FROM " + SAMTYKKE_TABELL);
    }

    private Map<String, Object> mapTilDatabaseParametre(Samtykke samtykke) {
        return new HashMap() {
            {
                put(AKTOER_ID, samtykke.getAktorId());
                put(FOEDSELSNUMMER, samtykke.getFoedselsnummer());
                put(GJELDER, samtykke.getGjelder());
                put(OPPRETTET_TIDSPUNKT, samtykke.getOpprettetTidspunkt());
                put(ENDRING, samtykke.getEndring());
            }
        };
    }

    public void slettSamtykkeForCV(String aktoerId) {
        String delete = String.format("DELETE FROM " + SAMTYKKE_TABELL + " where " + GJELDER + " = '%s' and " + AKTOER_ID + " = '%s'", SAMTYKKE_CV, aktoerId);
        jdbcTemplate.execute(delete);
    }

    private class SamtykkeMapper implements RowMapper<Samtykke> {

        @Override
        public Samtykke mapRow(ResultSet rs, int i) throws SQLException {

            return Samtykke.builder()
                    .aktorId(rs.getString(AKTOER_ID))
                    .foedselsnummer(rs.getString(FOEDSELSNUMMER))
                    .gjelder(rs.getString(GJELDER))
                    .opprettetTidspunkt(konverter(rs.getTimestamp(OPPRETTET_TIDSPUNKT)))
                    .build();
        }
    }

    private static LocalDateTime konverter(Timestamp timestamp) {
        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }
}
