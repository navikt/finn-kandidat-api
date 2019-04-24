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
                .fysiskeBehov(stringTilListe(rs.getString(FYSISKE_BEHOV), FysiskBehov.class))
                .arbeidsmiljoBehov(stringTilListe(rs.getString(ARBEIDSMILJO_BEHOV), ArbeidsmiljoBehov.class))
                .grunnleggendeBehov(stringTilListe(rs.getString(GRUNNLEGGENDE_BEHOV), GrunnleggendeBehov.class))
                .build();
    }

    private static <E extends Enum<E>> List<E> stringTilListe(String string, Class<E> klasse) {
        if (string == null) {
            return new ArrayList<>();
        }
        return Stream.of(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(name -> E.valueOf(klasse, name))
                .collect(Collectors.toList());
    }

    static <E extends Enum<E>> String listeTilString(List<E> list) {
        if (list == null) {
            return null;
        }
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
