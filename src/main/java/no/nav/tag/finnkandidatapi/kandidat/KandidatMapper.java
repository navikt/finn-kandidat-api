package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.*;

public class KandidatMapper implements RowMapper<Kandidat> {

    @Override
    public Kandidat mapRow(ResultSet rs, int i) throws SQLException {

        LocalDateTime sistEndret = rs.getTimestamp(REGISTRERINGSTIDSPUNKT) == null ? null : rs.getTimestamp(REGISTRERINGSTIDSPUNKT).toLocalDateTime();
        ArbeidstidBehov arbeidstidBehov = rs.getString(ARBEIDSTID_BEHOV) == null ? null : ArbeidstidBehov.valueOf(rs.getString(ARBEIDSTID_BEHOV));

        return Kandidat.builder()
                .id(rs.getInt(ID))
                .fnr(rs.getString(FNR))
                .sistEndretAv(rs.getString(REGISTRERT_AV))
                .sistEndret(sistEndret)
                .arbeidstidBehov(arbeidstidBehov)
                .fysiskeBehov(tilFysisk(rs.getString(FYSISKE_BEHOV)))
                .arbeidsmiljoBehov(tilArbeidsmiljo(rs.getString(ARBEIDSMILJO_BEHOV)))
                .grunnleggendeBehov(tilGrunnleggende(rs.getString(GRUNNLEGGENDE_BEHOV)))
                .build();
    }

    private static List<FysiskBehov> tilFysisk(String string) {
        if (string == null) {
            return new ArrayList<>();
        }
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(FysiskBehov::valueOf)
                .collect(Collectors.toList());
    }

    static String fraFysisk(List<FysiskBehov> list) {
        if (list == null) {
            return null;
        }
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private static List<ArbeidsmiljoBehov> tilArbeidsmiljo(String string) {
        if (string == null) {
            return new ArrayList<>();
        }
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(ArbeidsmiljoBehov::valueOf)
                .collect(Collectors.toList());
    }

    static String fraArbeidsmiljo(List<ArbeidsmiljoBehov> list) {
        if (list == null) {
            return null;
        }
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }

    private static List<GrunnleggendeBehov> tilGrunnleggende(String string) {
        if (string == null) {
            return new ArrayList<>();
        }
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(GrunnleggendeBehov::valueOf)
                .collect(Collectors.toList());
    }

    static String fraGrunnleggende(List<GrunnleggendeBehov> list) {
        if (list == null) {
            return null;
        }
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
