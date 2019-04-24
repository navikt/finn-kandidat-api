package no.nav.tag.finnkandidatapi.tilretteleggingsbehov;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TilretteleggingsbehovMapper implements RowMapper<Tilretteleggingsbehov> {

    @Override
    public Tilretteleggingsbehov mapRow(ResultSet rs, int i) throws SQLException {

        LocalDateTime opprettet = rs.getTimestamp("opprettet") == null ? null : rs.getTimestamp("opprettet").toLocalDateTime();
        Arbeidstid arbeidstid = rs.getString("arbeidstid") == null ? null : Arbeidstid.valueOf(rs.getString("arbeidstid"));

        return Tilretteleggingsbehov.builder()
                .id(rs.getInt("id"))
                .opprettet(opprettet)
                .opprettetAvIdent(rs.getString("opprettet_av_ident"))
                .brukerFnr(rs.getString("bruker_fnr"))
                .arbeidstid(arbeidstid)
                .fysisk(tilFysisk(rs.getString("fysisk")))
                .arbeidsmiljo(tilArbeidsmiljo(rs.getString("arbeidsmiljo")))
                .grunnleggende(tilGrunnleggende(rs.getString("grunnleggende")))
                .build();
    }

    private static List<Fysisk> tilFysisk(String string) {
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(Fysisk::valueOf)
                .collect(Collectors.toList());
    }

    static String fraFysisk(List<Fysisk> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private static List<Arbeidsmiljo> tilArbeidsmiljo(String string) {
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(Arbeidsmiljo::valueOf)
                .collect(Collectors.toList());
    }

    static String fraArbeidsmiljo(List<Arbeidsmiljo> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private static List<Grunnleggende> tilGrunnleggende(String string) {
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(Grunnleggende::valueOf)
                .collect(Collectors.toList());
    }

    static String fraGrunnleggende(List<Grunnleggende> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
