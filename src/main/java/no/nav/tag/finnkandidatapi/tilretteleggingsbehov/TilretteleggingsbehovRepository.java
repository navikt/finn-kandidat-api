package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.TilretteleggingsbehovMapper.*;

@Repository
public class TilretteleggingsbehovRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TilretteleggingsbehovRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Integer lagreTilretteleggingsbehov(Tilretteleggingsbehov tilretteleggingsbehov) {
        return jdbcTemplate.update("INSERT INTO tilretteleggingsbehov(opprettet, opprettet_av_ident, bruker_fnr, arbeidstid, fysisk, arbeidsmiljo, grunnleggende) VALUES (?, ?, ?, ?, ?, ?, ?)",
                tilretteleggingsbehov.getOpprettet(),
                tilretteleggingsbehov.getOpprettetAvIdent(),
                tilretteleggingsbehov.getBrukerFnr(),
                tilretteleggingsbehov.getArbeidstid().name(),
                fraFysisk(tilretteleggingsbehov.getFysisk()),
                fraArbeidsmiljo(tilretteleggingsbehov.getArbeidsmiljo()),
                fraGrunnleggende(tilretteleggingsbehov.getGrunnleggende())
        );
    }

    Tilretteleggingsbehov hentTilretteleggingsbehov(Integer id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM tilretteleggingsbehov WHERE id = ?", new Object[] { id },
                TilretteleggingsbehovMapper::map
        );
    }

}
