package no.nav.tag.finnkandidatapi.kandidat;

import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class KafkaKandidatMapper extends KandidatMapper {
    @Override
    public Kandidat mapRow(ResultSet rs, int i) throws SQLException {
        return mapKandidat(rs, i);
    }
}
