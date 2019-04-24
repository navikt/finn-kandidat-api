package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
                .fysiskeBehov(stringTilEnumSet(rs.getString(FYSISKE_BEHOV), FysiskBehov.class))
                .arbeidsmiljoBehov(stringTilEnumSet(rs.getString(ARBEIDSMILJO_BEHOV), ArbeidsmiljoBehov.class))
                .grunnleggendeBehov(stringTilEnumSet(rs.getString(GRUNNLEGGENDE_BEHOV), GrunnleggendeBehov.class))
                .build();
    }

    private static <E extends Enum<E>> Set<E> stringTilEnumSet(String string, Class<E> klasse) {
        if (string == null) {
            return new HashSet<>();
        }
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(name -> E.valueOf(klasse, name))
                .collect(Collectors.toSet());
    }

    static <E extends Enum<E>> String enumSetTilString(Set<E> set) {
        if (set == null) {
            return null;
        }
        String[] stringlist = set.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
