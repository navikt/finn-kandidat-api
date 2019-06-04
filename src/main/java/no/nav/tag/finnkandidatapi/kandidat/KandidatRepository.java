package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatMapper.*;

@Repository
public class KandidatRepository {

    static final String KANDIDAT_TABELL = "kandidat";
    static final String ID = "id";
    static final String FNR = "fnr";
    static final String REGISTRERT_AV = "registrert_av";
    static final String REGISTRERINGSTIDSPUNKT = "registreringstidspunkt";
    static final String ARBEIDSTID_BEHOV = "arbeidstid_behov";
    static final String FYSISKE_BEHOV = "fysiske_behov";
    static final String ARBEIDSMILJØ_BEHOV = "arbeidsmiljø_behov";
    static final String GRUNNLEGGENDE_BEHOV = "grunnleggende_behov";
    static final String SLETTET = "slettet";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;

    @Autowired
    public KandidatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(KANDIDAT_TABELL)
                .usingGeneratedKeyColumns(ID);
    }

    public Optional<Kandidat> hentNyesteKandidat(String fnr) {
        try {
            Kandidat kandidat = jdbcTemplate.queryForObject(
                    "SELECT * FROM kandidat WHERE (fnr = ? AND slettet = false) ORDER BY registreringstidspunkt DESC LIMIT 1", new Object[]{ fnr },
                    new KandidatMapper()
            );
            return Optional.of(kandidat);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Kandidat> hentKandidat(Integer id) {
        try {
            Kandidat kandidat = jdbcTemplate.queryForObject(
                    "SELECT * FROM kandidat WHERE id = ?", new Object[]{id},
                    new KandidatMapper()
            );
            return Optional.of(kandidat);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Kandidat> hentKandidater() {
        String query =
                "SELECT k.* " +
                "FROM kandidat k " +
                        "INNER JOIN " +
                        "(SELECT fnr, MAX(registreringstidspunkt) AS sisteRegistrert " +
                        "FROM kandidat " +
                        "GROUP BY fnr) gruppertKandidat " +
                        "ON k.fnr = gruppertKandidat.fnr " +
                        "AND k.registreringstidspunkt = gruppertKandidat.sisteRegistrert " +
                "WHERE slettet = false " +
                "ORDER BY k.registreringstidspunkt";
        return jdbcTemplate.query(query, new KandidatMapper());
    }

    public void slettAlleKandidater() {
        jdbcTemplate.execute("DELETE FROM kandidat");
    }

    public Integer lagreKandidat(Kandidat kandidat) {
        Map<String, Object> parameters = lagInsertParameter(kandidat);
        return jdbcInsert.executeAndReturnKey(parameters).intValue();
    }

    private Map<String, Object> lagInsertParameter(Kandidat kandidat) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FNR, kandidat.getFnr());
        parameters.put(REGISTRERT_AV, kandidat.getSistEndretAv());
        parameters.put(REGISTRERINGSTIDSPUNKT, kandidat.getSistEndret());
        parameters.put(ARBEIDSTID_BEHOV, kandidat.getArbeidstidBehov() == null ? null : kandidat.getArbeidstidBehov().name());
        parameters.put(FYSISKE_BEHOV, enumSetTilString(kandidat.getFysiskeBehov()));
        parameters.put(ARBEIDSMILJØ_BEHOV, enumSetTilString(kandidat.getArbeidsmiljøBehov()));
        parameters.put(GRUNNLEGGENDE_BEHOV, enumSetTilString(kandidat.getGrunnleggendeBehov()));
        parameters.put(SLETTET, false);
        return parameters;
    }

    public Integer slettKandidat(String fnr) {
        return jdbcTemplate.update("DELETE FROM kandidat WHERE fnr = ?", new Object[]{fnr});
    }

    public Integer markerKandidatSomSlettet(String fnr) {
        return jdbcTemplate.update("UPDATE kandidat SET slettet = true WHERE fnr = ?", new Object[]{fnr});
    }
}
