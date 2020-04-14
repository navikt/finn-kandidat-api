package no.nav.finnkandidatapi.vedtak;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class VedtakRepository {

    static final String VEDTAK_TABELL = "vedtak";
    static final String ID = "id";
    static final String AKTØR_ID = "aktor_id";
    static final String FNR = "fnr";
    static final String OPPRETTET = "opprettet";
    static final String SLETTET = "slettet";
    static final String VEDTAK_ID = "vedtak_id";
    static final String PERSON_ID = "person_id";
    static final String TYPEKODE = "vedtaktypekode";
    static final String STATUSKODE = "vedtakstatuskode";
    static final String UTFALLKODE = "utfallkode";
    static final String RETTIGHETKODE = "rettighetkode";
    static final String FRA_DATO = "fra_dato";
    static final String TIL_DATO = "til_dato";
    static final String ARENADB_TS = "ts_fra_arena";
    static final String ARENADB_POS = "pos_fra_arena";
    static final String ARENADB_OP = "op_fra_arena";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    private final VedtakMapper vedtakMapper;
    private final DateProvider dateProvider;

    @Autowired
    public VedtakRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert simpleJdbcInsert, VedtakMapper vedtakMapper, DateProvider dateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = simpleJdbcInsert
                .withTableName(VEDTAK_TABELL)
                .usingGeneratedKeyColumns(ID);
        this.vedtakMapper = vedtakMapper;
        this.dateProvider = dateProvider;
    }

    public Optional<Vedtak> hentNyesteVersjonAvNyesteVedtakForAktør(String aktørId) {
        try {
            Vedtak vedtak = jdbcTemplate.queryForObject(
                    "SELECT * FROM vedtak WHERE aktor_id = ? AND slettet = false ORDER BY fra_dato DESC, id DESC LIMIT 1", new Object[]{aktørId},
                    vedtakMapper
            );
            return Optional.ofNullable(vedtak);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Vedtak> hentNyesteVedtak(String vedtakId) {
        try {
            Vedtak vedtak = jdbcTemplate.queryForObject(
                    "SELECT * FROM vedtak WHERE vedtak_id = ? ORDER BY id DESC LIMIT 1", new Object[]{vedtakId},
                    vedtakMapper
            );
            return Optional.ofNullable(vedtak);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Vedtak> hentVedtak(Long id) {
        try {
            Vedtak vedtak = jdbcTemplate.queryForObject(
                    "SELECT * FROM vedtak WHERE id = ?", new Object[]{id},
                    vedtakMapper
            );
            return Optional.ofNullable(vedtak);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long lagreVedtak(Vedtak vedtak) {
        Map<String, Object> parameters = lagInsertParameter(vedtak);
        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    public int logiskSlettVedtak(Vedtak vedtak) {
        try {
            return jdbcTemplate.update("UPDATE vedtak SET slettet = true WHERE vedtak_id = ?", vedtak.getVedtakId());
        } catch (DataAccessException e) {
            log.error("Klarte ikke å sette vedtak {} som slettet", vedtak.getVedtakId());
            return 0;
        }
    }

    private Map<String, Object> lagInsertParameter(Vedtak vedtak) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(OPPRETTET, dateProvider.now());
        parameters.put(AKTØR_ID, vedtak.getAktørId());
        parameters.put(FNR, vedtak.getFnr());
        parameters.put(VEDTAK_ID, vedtak.getVedtakId());
        parameters.put(PERSON_ID, vedtak.getPersonId());
        parameters.put(TYPEKODE, vedtak.getTypeKode());
        parameters.put(STATUSKODE, vedtak.getStatusKode());
        parameters.put(UTFALLKODE, vedtak.getUtfallKode());
        parameters.put(RETTIGHETKODE, vedtak.getRettighetKode());
        parameters.put(FRA_DATO, vedtak.getFraDato());
        parameters.put(TIL_DATO, vedtak.getTilDato());
        parameters.put(ARENADB_TS, vedtak.getArenaDbTidsstempel());
        parameters.put(ARENADB_POS, vedtak.getArenaDbTransactionlogPosisjon());
        parameters.put(ARENADB_OP, vedtak.getArenaDbOperasjon());
        parameters.put(SLETTET, vedtak.isSlettet());

        return parameters;
    }

    public void fysiskSlettAlleVedtak() {
        jdbcTemplate.execute("DELETE FROM vedtak" );
    }
}
