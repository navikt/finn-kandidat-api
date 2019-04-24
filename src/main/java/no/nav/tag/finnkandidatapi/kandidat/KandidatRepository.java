package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

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
    static final String ARBEIDSMILJO_BEHOV = "arbeidsmiljo_behov";
    static final String GRUNNLEGGENDE_BEHOV = "grunnleggende_behov";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;

    @Autowired
    public KandidatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(KANDIDAT_TABELL)
                .usingGeneratedKeyColumns(ID);
    }

    Integer lagreKandidat(Kandidat kandidat) {
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
        parameters.put(ARBEIDSMILJO_BEHOV, enumSetTilString(kandidat.getArbeidsmiljoBehov()));
        parameters.put(GRUNNLEGGENDE_BEHOV, enumSetTilString(kandidat.getGrunnleggendeBehov()));
        return parameters;
    }

    Kandidat hentKandidat(Integer id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM kandidat WHERE id = ?", new Object[] { id },
                new KandidatMapper()
        );
    }

}
