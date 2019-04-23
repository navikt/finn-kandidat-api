package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                .fysisk(tilFysisk(rs.getString("fysisk")))
                .arbeidsmiljo(tilArbeidsmiljo(rs.getString("arbeidsmiljo")))
                .grunnleggende(tilGrunnleggende(rs.getString("grunnleggende")))
                .build();
    }

    private static ArrayList<Fysisk> tilFysisk(String string) {
        return Stream.of(string.split(","))
                .map(Fysisk::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static String fraFysisk(ArrayList<Fysisk> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private static ArrayList<Arbeidsmiljo> tilArbeidsmiljo(String string) {
        return Stream.of(string.split(","))
                .map(Arbeidsmiljo::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static String fraArbeidsmiljo(ArrayList<Arbeidsmiljo> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private static ArrayList<Grunnleggende> tilGrunnleggende(String string) {
        return Stream.of(string.split(","))
                .map(Grunnleggende::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static String fraGrunnleggende(ArrayList<Grunnleggende> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
