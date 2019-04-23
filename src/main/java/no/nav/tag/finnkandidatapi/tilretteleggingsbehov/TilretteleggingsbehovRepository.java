package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                (rs, rowNum) -> Tilretteleggingsbehov.builder()
                        .id(rs.getInt("id"))
                        .opprettet(rs.getTimestamp("opprettet").toLocalDateTime())
                        .opprettetAvIdent(rs.getString("opprettet_av_ident"))
                        .brukerFnr(rs.getString("bruker_fnr"))
                        .arbeidstid(Arbeidstid.valueOf(rs.getString("arbeidstid")))
                        .fysisk(tilFysisk(rs.getString("fysisk")))
                        .arbeidsmiljo(tilArbeidsmiljo(rs.getString("arbeidsmiljo")))
                        .grunnleggende(tilGrunnleggende(rs.getString("grunnleggende")))
                        .build()
        );
    }

    private ArrayList<Fysisk> tilFysisk(String string) {
        return Stream.of(string.split(","))
                .map(Fysisk::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String fraFysisk(ArrayList<Fysisk> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private ArrayList<Arbeidsmiljo> tilArbeidsmiljo(String string) {
        return Stream.of(string.split(","))
                .map(Arbeidsmiljo::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String fraArbeidsmiljo(ArrayList<Arbeidsmiljo> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private ArrayList<Grunnleggende> tilGrunnleggende(String string) {
        return Stream.of(string.split(","))
                .map(Grunnleggende::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String fraGrunnleggende(ArrayList<Grunnleggende> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
