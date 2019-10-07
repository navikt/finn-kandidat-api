package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatRepository.SLETTET;

@Component
public class KafkaKandidatMapper implements RowMapper<KafkaKandidat> {
    @Override
    public KafkaKandidat mapRow(ResultSet rs, int i) throws SQLException {
        Kandidat kandidat = KandidatMapper.mapKandidat(rs, i);
        KafkaKandidat kafkaKandidat = KafkaKandidat.builder()
                .kandidat(kandidat)
                .slettet(rs.getBoolean(SLETTET))
                .build();

        return kafkaKandidat;
    }
}
