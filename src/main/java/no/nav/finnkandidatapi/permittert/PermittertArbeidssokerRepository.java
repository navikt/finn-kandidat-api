package no.nav.finnkandidatapi.permittert;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.veilarboppfolging.VeilarbOppfolgingClient;
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
public class PermittertArbeidssokerRepository {

    static final String PERMITTERTARBEIDSSOKER_TABELL = "permittert";
    static final String ID = "id";
    static final String AKTØR_ID = "aktor_id";
    static final String STATUS_FRA_VEILARB_REGISTRERING = "status_fra_veilarb";
    static final String TIDSPUNKT_FOR_STATUS_FRA_VEILARB = "tidspunkt_for_status_fra_veilarb";
    static final String OPPRETTET = "opprettet";
    static final String SLETTET = "slettet";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    private final PermittertArbeidssokerMapper permittertArbeidssokerMapper;
    private final DateProvider dateProvider;

    @Autowired
    public PermittertArbeidssokerRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert simpleJdbcInsert, PermittertArbeidssokerMapper permittertArbeidssokerMapper, DateProvider dateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = simpleJdbcInsert
                .withTableName(PERMITTERTARBEIDSSOKER_TABELL)
                .usingGeneratedKeyColumns(ID);
        this.permittertArbeidssokerMapper = permittertArbeidssokerMapper;
        this.dateProvider = dateProvider;
    }

    public Optional<PermittertArbeidssoker> hentNyestePermittertArbeidssoker(String aktørId) {
        try {
            PermittertArbeidssoker permittert = jdbcTemplate.queryForObject(
                    "SELECT * FROM permittert WHERE (aktor_id = ?) ORDER BY id DESC LIMIT 1", new Object[]{aktørId},
                    permittertArbeidssokerMapper
            );
            return Optional.ofNullable(permittert);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<PermittertArbeidssoker> hentPermittertArbeidssoker(Integer id) {
        try {
            PermittertArbeidssoker permittert = jdbcTemplate.queryForObject(
                    "SELECT * FROM permittert WHERE id = ?", new Object[]{id},
                    permittertArbeidssokerMapper
            );
            return Optional.ofNullable(permittert);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer lagrePermittertArbeidssoker(PermittertArbeidssoker permittertArbeidssoker) {
        Map<String, Object> parameters = lagInsertParameter(permittertArbeidssoker, false);
        return jdbcInsert.executeAndReturnKey(parameters).intValue();
    }

    private Map<String, Object> lagInsertParameter(PermittertArbeidssoker permittertArbeidssoker, boolean skalSlettes) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AKTØR_ID, permittertArbeidssoker.getAktørId());
        parameters.put(STATUS_FRA_VEILARB_REGISTRERING, permittertArbeidssoker.getStatusFraVeilarbRegistrering());
        parameters.put(TIDSPUNKT_FOR_STATUS_FRA_VEILARB, permittertArbeidssoker.getTidspunktForStatusFraVeilarbRegistrering());
        parameters.put(SLETTET, skalSlettes);
        parameters.put(OPPRETTET, dateProvider.now());

        return parameters;
    }

    public Optional<Integer> slettPermittertArbeidssoker(String aktørId) {
        return hentNyestePermittertArbeidssoker(aktørId)
                .map(permittert -> lagInsertParameter(permittert, true))
                .map(parameters -> jdbcInsert.executeAndReturnKey(parameters).intValue());
    }

    public void slettAllePermitterteArbeidssokere() {
        jdbcTemplate.execute("DELETE FROM permittert" );
    }

    public List<PermittertArbeidssoker> hentAllePermitterteArbeidssokere() {
        return jdbcTemplate.query("SELECT * FROM permittert", permittertArbeidssokerMapper);
    }
}
