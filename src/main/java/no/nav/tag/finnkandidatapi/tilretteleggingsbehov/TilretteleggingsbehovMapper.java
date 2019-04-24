package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TilretteleggingsbehovMapper implements RowMapper<Tilretteleggingsbehov> {

    @Override
    public Tilretteleggingsbehov mapRow(ResultSet rs, int i) throws SQLException {
        return Tilretteleggingsbehov.builder()
                .id(rs.getInt("id"))
                .opprettet(rs.getTimestamp("opprettet").toLocalDateTime())
                .opprettetAvIdent(rs.getString("opprettet_av_ident"))
                .brukerFnr(rs.getString("bruker_fnr"))
                .arbeidstid(Arbeidstid.valueOf(rs.getString("arbeidstid")))
                .fysisk(stringTilListe(rs.getString("fysisk"), Fysisk.class))
                .arbeidsmiljo(stringTilListe(rs.getString("arbeidsmiljo"), Arbeidsmiljo.class))
                .grunnleggende(stringTilListe(rs.getString("grunnleggende"), Grunnleggende.class))
                .build();
    }

    private static <E extends Enum<E>> List<E> stringTilListe(String string, Class<E> klasse) {
        return Stream.of(string.split(","))
                .map(name -> E.valueOf(klasse, name))
                .collect(Collectors.toList());
    }

    static <E extends Enum<E>> String listeTilString(List<E> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
