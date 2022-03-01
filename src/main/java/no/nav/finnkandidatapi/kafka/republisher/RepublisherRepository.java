package no.nav.finnkandidatapi.kafka.republisher;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Brukertype;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Repository
public class RepublisherRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public RepublisherRepository(JdbcTemplate jdbcTemplate ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> hentAktørider() {
        String query = lagHentAktøridQuery();
        var liste =  jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("aktor_id"));
        return liste;
    }

    private String lagHentAktøridQuery() {
        return (
                "SELECT DISTINCT aktor_id " +
                "FROM kandidat " +
                "UNION " +
                "SELECT DISTINCT aktor_id " +
                "FROM permittert " +
                "UNION " +
                "SELECT DISTINCT aktor_id " +
                "FROM midlertidig_utilgjengelig " +
                "UNION " +
                "SELECT DISTINCT aktor_id " +
                "FROM vedtak "
        );
    }

    // Engangskjøringer for uttrekk. Endres etter behov.
    public List<String> hentCustomUtvalg() {
        String query = "SELECT DISTINCT aktor_id " +
                "FROM permittert " +
                "WHERE opprettet >'2022-02-28 00:00:00.000000' AND slettet=true order by opprettet desc";

        var liste =  jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("aktor_id"));
        return liste;
    }
}
