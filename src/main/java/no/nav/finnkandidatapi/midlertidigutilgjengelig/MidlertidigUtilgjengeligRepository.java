package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class MidlertidigUtilgjengeligRepository {

    static final String PERMITTERTARBEIDSSOKER_TABELL = "permittert";
    static final String ID = "id";
    static final String AKTØR_ID = "aktor_id";
    static final String FRA_DATO = "fra_dato";
    static final String TIL_DATO = "til_dato";
    static final String REGISTRERT_AV_IDENT = "registrert_av_ident";
    static final String REGISTRERT_AV_NAVN = "registrert_av_navn";
    static final String REGISTRERINGSTIDSPUNKT = "registreringstidspunkt";
    static final String SLETTET = "slettet";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    private final MidlertidigUtilgjengeligMapper utilgjengeligMapper;
    private final DateProvider dateProvider;

    @Autowired
    public MidlertidigUtilgjengeligRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert simpleJdbcInsert, MidlertidigUtilgjengeligMapper utilgjengeligMapper, DateProvider dateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = simpleJdbcInsert
                .withTableName(PERMITTERTARBEIDSSOKER_TABELL)
                .usingGeneratedKeyColumns(ID);
        this.utilgjengeligMapper = utilgjengeligMapper;
        this.dateProvider = dateProvider;
    }

    public Optional<MidlertidigUtilgjengelig> hentNyesteUtilgjengelig(String aktørId) {
        try {
            MidlertidigUtilgjengelig midlertidigUtilgjengelig = jdbcTemplate.queryForObject(
                    "SELECT * FROM utilgjengelig WHERE (aktor_id = ?) ORDER BY id DESC LIMIT 1", new Object[]{aktørId},
                    utilgjengeligMapper
            );
            return Optional.ofNullable(midlertidigUtilgjengelig);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<MidlertidigUtilgjengelig> hentUtilgjengelig(Integer id) {
        try {
            MidlertidigUtilgjengelig midlertidigUtilgjengelig = jdbcTemplate.queryForObject(
                    "SELECT * FROM utilgjengelig WHERE id = ?", new Object[]{id},
                    utilgjengeligMapper
            );
            return Optional.ofNullable(midlertidigUtilgjengelig);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer lagreUtilgjengelig(MidlertidigUtilgjengelig midlertidigUtilgjengelig) {
        Map<String, Object> parameters = lagInsertParameter(midlertidigUtilgjengelig, false);
        return jdbcInsert.executeAndReturnKey(parameters).intValue();
    }

    private Map<String, Object> lagInsertParameter(MidlertidigUtilgjengelig midlertidigUtilgjengelig, boolean skalSlettes) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AKTØR_ID, midlertidigUtilgjengelig.getAktørId());
        parameters.put(FRA_DATO, midlertidigUtilgjengelig.getFraDato());
        parameters.put(TIL_DATO, midlertidigUtilgjengelig.getTilDato());
        parameters.put(REGISTRERT_AV_IDENT, midlertidigUtilgjengelig.getRegistrertAvIdent());
        parameters.put(REGISTRERT_AV_NAVN, midlertidigUtilgjengelig.getRegistrertAvNavn());
        parameters.put(SLETTET, midlertidigUtilgjengelig.isSlettet());
        return parameters;
    }

    public Optional<Integer> slettUtilgjengelig(String aktørId) {
        return hentNyesteUtilgjengelig(aktørId)
                .map(midlertidigUtilgjengelig -> lagInsertParameter(midlertidigUtilgjengelig, true))
                .map(utilgjengelig -> jdbcInsert.executeAndReturnKey(utilgjengelig).intValue());
    }

    public void slettAllePermitterteArbeidssokere() {
        jdbcTemplate.execute("DELETE FROM utilgjengelig");
    }

    public List<MidlertidigUtilgjengelig> hentAlleUtilgjengelig() {
        return jdbcTemplate.query(
                "SELECT p.* " +
                "FROM utilgjengelig p " +
                "INNER JOIN " +
                "(SELECT aktor_id, MAX(id) AS nyesteId " +
                "FROM utilgjengelig " +
                "GROUP BY aktor_id) gruppertUtilgjengelig " +
                "ON p.aktor_id = gruppertUtilgjengelig.aktor_id " +
                "AND p.id = gruppertUtilgjengelig.nyesteId " +
                "WHERE slettet = false " +
                "ORDER BY p.registreringstidspunkt",
                utilgjengeligMapper);
    }
}
