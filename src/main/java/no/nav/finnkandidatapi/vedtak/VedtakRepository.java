package no.nav.finnkandidatapi.vedtak;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
    static final String SAK_ID = "sak_id";
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

    public List<Vedtak> hentNyesteVedtakForAktør(String aktørId) {
        try {
            return jdbcTemplate.query(
                    "SELECT v1.* FROM vedtak v1 WHERE (v1.aktor_id = ?) AND NOT EXISTS (SELECT 1 FROM vedtak v2 where v2.id > v1.id AND v2.aktor_id = v1.aktor_id AND v2.vedtak_id = v1.vedtak_id)",
                    vedtakMapper,
                    new Object[]{aktørId}
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public Optional<Vedtak> hentNyesteVedtak(String vedtakId) {
        try {
            Vedtak vedtak = jdbcTemplate.queryForObject(
                    "SELECT * FROM vedtak WHERE (vedtak_id = ?) ORDER BY id DESC LIMIT 1", new Object[]{vedtakId},
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

    private Map<String, Object> lagInsertParameter(Vedtak vedtak) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(OPPRETTET, dateProvider.now());
        parameters.put(AKTØR_ID, vedtak.getAktørId());
        parameters.put(FNR, vedtak.getFnr());
        parameters.put(VEDTAK_ID, vedtak.getVedtakId());
        parameters.put(SAK_ID, vedtak.getSakId());
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
