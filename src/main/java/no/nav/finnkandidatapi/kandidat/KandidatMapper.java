package no.nav.finnkandidatapi.kandidat;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KandidatMapper implements RowMapper<Kandidat> {

    @Override
    public Kandidat mapRow(ResultSet rs, int i) throws SQLException {
        if (rs.getBoolean(KandidatRepository.SLETTET)) {
            return null;
        }

        return mapKandidat(rs, i);
    }

    public static Kandidat mapKandidat(ResultSet rs, int i) throws SQLException {
        LocalDateTime sistEndret = rs.getTimestamp(KandidatRepository.REGISTRERINGSTIDSPUNKT) == null ? null : rs.getTimestamp(KandidatRepository.REGISTRERINGSTIDSPUNKT).toLocalDateTime();

        return Kandidat.builder()
                .id(rs.getInt(KandidatRepository.ID))
                .fnr(rs.getString(KandidatRepository.FNR))
                .aktørId(rs.getString(KandidatRepository.AKTØR_ID))
                .sistEndretAv(rs.getString(KandidatRepository.REGISTRERT_AV))
                .sistEndret(sistEndret)
                .arbeidstidBehov(stringTilEnumSet(rs.getString(KandidatRepository.ARBEIDSTID_BEHOV), ArbeidstidBehov.class))
                .fysiskeBehov(stringTilEnumSet(rs.getString(KandidatRepository.FYSISKE_BEHOV), FysiskBehov.class))
                .arbeidsmiljøBehov(stringTilEnumSet(rs.getString(KandidatRepository.ARBEIDSMILJØ_BEHOV), ArbeidsmiljøBehov.class))
                .grunnleggendeBehov(stringTilEnumSet(rs.getString(KandidatRepository.GRUNNLEGGENDE_BEHOV), GrunnleggendeBehov.class))
                .navKontor(rs.getString(KandidatRepository.NAV_KONTOR))
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
