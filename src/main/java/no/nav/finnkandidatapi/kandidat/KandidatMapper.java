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

import static no.nav.finnkandidatapi.kandidat.KandidatRepository.*;

@Component
public class KandidatMapper implements RowMapper<Kandidat> {

    @Override
    public Kandidat mapRow(ResultSet rs, int i) throws SQLException {
        if (rs.getBoolean(SLETTET)) {
            return null;
        }

        return mapKandidat(rs, i);
    }

    public static Kandidat mapKandidat(ResultSet rs, int i) throws SQLException {
        LocalDateTime sistEndret = rs.getTimestamp(REGISTRERINGSTIDSPUNKT) == null ? null : rs.getTimestamp(REGISTRERINGSTIDSPUNKT).toLocalDateTime();

        return Kandidat.builder()
                .id(rs.getInt(ID))
                .fnr(rs.getString(FNR))
                .aktørId(rs.getString(AKTØR_ID))
                .sistEndretAv(rs.getString(REGISTRERT_AV))
                .sistEndretAvVeileder(sistEndret)
                .arbeidstid(stringTilEnumSet(rs.getString(ARBEIDSTID_BEHOV), Arbeidstid.class))
                .fysisk(stringTilEnumSet(rs.getString(FYSISKE_BEHOV), Fysisk.class))
                .arbeidshverdagen(stringTilEnumSet(rs.getString(ARBEIDSHVERDAGEN_BEHOV), Arbeidshverdagen.class))
                .utfordringerMedNorsk(stringTilEnumSet(rs.getString(UTFORDRINGERMEDNORSK_BEHOV), UtfordringerMedNorsk.class))
                .navKontor(rs.getString(NAV_KONTOR))
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
