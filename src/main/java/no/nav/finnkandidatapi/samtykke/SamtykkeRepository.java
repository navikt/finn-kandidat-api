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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SamtykkeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final SamtykkeMapper samtykkeMapper;

    static final String SAMTYKKE_TABELL = "samtykke";
    private final String FOEDSELSNUMMER = "foedselsnummer";
    private final String GJELDER = "gjelder";
    private final String OPPRETTET_TIDSPUNKT = "opprettet_tidspunkt";

    private final String SAMTYKKE_CV = "CV_HJEMMEL";

    static final String ID = "id";

    @Autowired
    public SamtykkeRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert jdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = jdbcInsert
                .withTableName(SAMTYKKE_TABELL)
                .usingGeneratedKeyColumns(ID);
        samtykkeMapper = new SamtykkeMapper();
    }

    public Optional<Samtykke> hentSamtykkeForCV(String foedselsnummer) {
        try {
            Samtykke samtykke = jdbcTemplate.queryForObject("SELECT * from " + SAMTYKKE_TABELL +
                            " where " + FOEDSELSNUMMER + " = ? and " + GJELDER + " = '" + SAMTYKKE_CV + "'",
                    new Object[]{foedselsnummer}, samtykkeMapper);
            return Optional.ofNullable(samtykke);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void oppdaterGittSamtykke(Samtykke samtykke) {
        String update = "UPDATE " + SAMTYKKE_TABELL +
                " SET " + OPPRETTET_TIDSPUNKT + " = ?" +
                " WHERE " + FOEDSELSNUMMER + "= ? AND " + GJELDER + "= ?;";

        jdbcTemplate.update(update,
                samtykke.getOpprettetTidspunkt(),
                samtykke.getFoedselsnummer(),
                samtykke.getGjelder());
    }

    public void lagreSamtykke(Samtykke samtykke) {
        jdbcInsert.execute(mapTilDatabaseParametre(samtykke));
    }

    public boolean harSamtykkeForCV(String foedselsnummer) {
        return hentSamtykkeForCV(foedselsnummer).isPresent();
    }

    List<Samtykke> hentAlleSamtykker() {
        return jdbcTemplate.query("SELECT * FROM " + SAMTYKKE_TABELL, samtykkeMapper);
    }

    void slettAlleSamtykker() {
        jdbcTemplate.execute("DELETE FROM " + SAMTYKKE_TABELL);
    }

    private Map<String, Object> mapTilDatabaseParametre(Samtykke samtykke) {
        return Map.of(
                FOEDSELSNUMMER, samtykke.getFoedselsnummer(),
                GJELDER, samtykke.getGjelder(),
                OPPRETTET_TIDSPUNKT, samtykke.getOpprettetTidspunkt()
        );
    }

    public void slettSamtykkeForCV(String aktoerId) {
        String delete = String.format("DELETE FROM " + SAMTYKKE_TABELL + " where " + GJELDER + " = '%s' and " + FOEDSELSNUMMER + " = '%s'", SAMTYKKE_CV, aktoerId);
        jdbcTemplate.execute(delete);
    }

    private class SamtykkeMapper implements RowMapper<Samtykke> {

        @Override
        public Samtykke mapRow(ResultSet rs, int i) throws SQLException {

            return Samtykke.builder()
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
