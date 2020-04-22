package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.kandidat.Veileder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Repository
public class MidlertidigUtilgjengeligRepository {

    static final String MIDLERTIDIG_UTILGJENGELIG_TABELL = "midlertidig_utilgjengelig";
    static final String ID = "id";
    static final String AKTØR_ID = "aktor_id";
    static final String FRA_DATO = "fra_dato";
    static final String TIL_DATO = "til_dato";
    static final String REGISTRERT_AV_IDENT = "registrert_av_ident";
    static final String REGISTRERT_AV_NAVN = "registrert_av_navn";
    static final String SIST_ENDRET_AV_IDENT = "sist_endret_av_ident";
    static final String SIST_ENDRET_AV_NAVN = "sist_endret_av_navn";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    private final MidlertidigUtilgjengeligMapper midlertidigUtilgjengeligMapper;

    @Autowired
    public MidlertidigUtilgjengeligRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert simpleJdbcInsert, MidlertidigUtilgjengeligMapper midlertidigUtilgjengeligMapper, DateProvider dateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = simpleJdbcInsert
                .withTableName(MIDLERTIDIG_UTILGJENGELIG_TABELL)
                .usingGeneratedKeyColumns(ID);
        this.midlertidigUtilgjengeligMapper = midlertidigUtilgjengeligMapper;
    }

    public Optional<MidlertidigUtilgjengelig> hentMidlertidigUtilgjengeligMedId(Integer id) {
        try {
            MidlertidigUtilgjengelig midlertidigUtilgjengelig = jdbcTemplate.queryForObject(
                    "SELECT * FROM " + MIDLERTIDIG_UTILGJENGELIG_TABELL + " WHERE id = ?", new Object[]{id},
                    midlertidigUtilgjengeligMapper
            );
            return Optional.ofNullable(midlertidigUtilgjengelig);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<MidlertidigUtilgjengelig> hentMidlertidigUtilgjengelig(String aktørId) {
        try {
            MidlertidigUtilgjengelig midlertidigUtilgjengelig = jdbcTemplate.queryForObject(
                    "SELECT * FROM " + MIDLERTIDIG_UTILGJENGELIG_TABELL + " WHERE aktor_id = ?", new Object[]{aktørId},
                    midlertidigUtilgjengeligMapper
            );
            return Optional.ofNullable(midlertidigUtilgjengelig);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer lagreMidlertidigUtilgjengelig(MidlertidigUtilgjengelig midlertidigUtilgjengelig) {
        Map<String, Object> parameters = lagInsertParameter(midlertidigUtilgjengelig);
        return jdbcInsert.executeAndReturnKey(parameters).intValue();
    }

    private Map<String, Object> lagInsertParameter(MidlertidigUtilgjengelig midlertidigUtilgjengelig) {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put(AKTØR_ID, midlertidigUtilgjengelig.getAktørId());
        parameters.put(FRA_DATO, midlertidigUtilgjengelig.getFraDato());
        parameters.put(TIL_DATO, midlertidigUtilgjengelig.getTilDato());
        parameters.put(REGISTRERT_AV_IDENT, midlertidigUtilgjengelig.getRegistrertAvIdent());
        parameters.put(REGISTRERT_AV_NAVN, midlertidigUtilgjengelig.getRegistrertAvNavn());

        return parameters;
    }

    public Integer endreMidlertidigUtilgjengelig(String aktørId, LocalDateTime nyDato, Veileder innloggetVeileder) {
        return jdbcTemplate.update(
                "UPDATE " + MIDLERTIDIG_UTILGJENGELIG_TABELL +
                        " SET til_dato = ?, sist_endret_av_ident = ?, sist_endret_av_navn = ?" +
                        " WHERE aktor_id = ?",
                nyDato,
                innloggetVeileder.getNavIdent(),
                innloggetVeileder.getNavn(),
                aktørId);
    }

    public Integer slettMidlertidigUtilgjengelig(String aktørId) {
        return jdbcTemplate.update("DELETE FROM " + MIDLERTIDIG_UTILGJENGELIG_TABELL + " WHERE aktor_id = ?", new Object[]{aktørId});
    }

    public List<MidlertidigUtilgjengelig> hentAlleMidlertidigUtilgjengelig() {
        try {
            return jdbcTemplate.query(
                    "SELECT * FROM " + MIDLERTIDIG_UTILGJENGELIG_TABELL,
                    midlertidigUtilgjengeligMapper
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
}
